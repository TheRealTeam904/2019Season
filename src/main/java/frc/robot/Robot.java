/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.sensors.PigeonIMU_StatusFrame;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import com.ctre.phoenix.sensors.PigeonIMU;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  TalonSRX _LeftMaster = new TalonSRX(6);
	TalonSRX _LeftSlave = new TalonSRX(7);

	TalonSRX _RightMaster = new TalonSRX(4);
  TalonSRX _RightSlave = new TalonSRX(5);

	TalonSRX _Ball = new TalonSRX(8);
	TalonSRX _Climber = new TalonSRX(9);
	TalonSRX _FourBar = new TalonSRX(10);

	PigeonIMU _Pidgey = new PigeonIMU(10);

  
	Joystick _Driver = new Joystick(0);
  Joystick _Operator = new Joystick(1);

  double FourBar = -1 * _Operator.getRawAxis(5);
  double BallOutDriver = _Driver.getRawAxis(2);
  double BallOutOperator = _Operator.getRawAxis(2);
  double BallInOperator = _Operator.getRawAxis(3);

  public static Compressor Compressor = new Compressor(0);
  
  public static DoubleSolenoid Pivot = new DoubleSolenoid(0, 1);
	public static DoubleSolenoid.Value PivotUp = DoubleSolenoid.Value.kReverse;
  public static DoubleSolenoid.Value PivotDown = DoubleSolenoid.Value.kForward;
  
  boolean PivotedUp = true;

	public static DoubleSolenoid BackClimber = new DoubleSolenoid(2, 3);
	public static DoubleSolenoid.Value BackClimberClose = DoubleSolenoid.Value.kReverse;
	public static DoubleSolenoid.Value BackClimberExtended = DoubleSolenoid.Value.kForward;

  /*
	public static DoubleSolenoid Eject = new DoubleSolenoid(4, 5);
	public static DoubleSolenoid.Value EjectOut = DoubleSolenoid.Value.kReverse;
	public static DoubleSolenoid.Value EjectIn = DoubleSolenoid.Value.kForward;
*/
  
  public double deadzone(double x) {
		if(x > 0.20)
			x = (x - 0.20) * 1.25;
		else if(x < -0.20)
			x = (x + 0.20) * 1.25;
		else
			x = 0;
		return x;
	}
	
	public double[] deadzone(double x, double y) {	
		return new double[] {(deadzone(x) * 0.75), (deadzone(y))};
  }
  
  
  public void drive(double turn, double forward) {
		double DriveRight = (forward - turn);
    double DriveLeft = (forward + turn );
    
    _LeftMaster.set(ControlMode.PercentOutput, DriveLeft);
    _RightMaster.set(ControlMode.PercentOutput, DriveRight);
	}
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {

    _LeftSlave.follow(_LeftMaster);
    _RightSlave.follow(_RightMaster);
   
    _LeftMaster.setNeutralMode(NeutralMode.Brake);
    _LeftSlave.setNeutralMode(NeutralMode.Brake);
    _RightMaster.setNeutralMode(NeutralMode.Brake);
    _RightSlave.setNeutralMode(NeutralMode.Brake);
    _Climber.setNeutralMode(NeutralMode.Brake);

    _LeftMaster.setInverted(false);
    _LeftSlave.setInverted(false);
    _RightMaster.setInverted(true);
    _RightSlave.setInverted(true);
    
		_LeftMaster.setSensorPhase(false);
    _RightMaster.setSensorPhase(false);

    _LeftMaster.configPeakOutputForward(+1.0, 30);
		_LeftMaster.configPeakOutputReverse(-1.0, 30);
		_RightMaster.configPeakOutputForward(+1.0, 30);
		_RightMaster.configPeakOutputReverse(-1.0, 30);

    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser); 
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    double[] xy = deadzone( _Driver.getRawAxis(4), -1 * _Driver.getRawAxis(1));
    drive(xy[0], xy[1]);
    _Climber.set(ControlMode.PercentOutput, 0);
    _Ball.set(ControlMode.PercentOutput, .0);
    /*if(_Driver.getRawButton(6)){
			//Abort...
			System.out.println("Abort...\n");
			_RightMaster.set(ControlMode.PercentOutput, 0);
			_LeftMaster.set(ControlMode.PercentOutput, 0);
			_Ball.set(ControlMode.PercentOutput, 0);
			_Climber.set(ControlMode.PercentOutput, 0);
			_FourBar.set(ControlMode.PercentOutput, 0);
    }*/
    
    //// Driver Controls
			if(_Driver.getRawButton(1)){
        _Climber.set(ControlMode.PercentOutput, .75);
      }
      if(_Driver.getRawButton(2)){
        _Climber.set(ControlMode.PercentOutput, -.2);
      }
      if(_Driver.getRawButton(3)){
        BackClimber.set(BackClimberExtended);
			}
			if(BallOutDriver >= 0.5){
				_Ball.set(ControlMode.PercentOutput, .5);
			}

      //// Operator Controls

      _FourBar.set(ControlMode.PercentOutput, -1 * _Operator.getRawAxis(5));
			
			if(_Operator.getRawAxis(2) >= 0.5){
				_Ball.set(ControlMode.PercentOutput, .45);
			}
			if(_Operator.getRawAxis(3) >= 0.5){
				_Ball.set(ControlMode.PercentOutput, -.5);
			}
			if(_Operator.getRawButton(6)){
				//Eject.set(EjectIn);
      }
      
			if(_Operator.getRawButton(5)){
				//Eject.set(EjectOut);
      }
      
			if(_Operator.getRawButton(3)){
          Pivot.set(DoubleSolenoid.Value.kReverse);
      }
      if(_Operator.getRawButton(4)){
        Pivot.set(DoubleSolenoid.Value.kForward);
    }
      
			if(_Operator.getRawButton(1)){
				//GamePiece Presets Low (Hatch)
				if(_Operator.getRawButton(7)){
					//GamePiece Presets Low (Cargo)
				}
      }
      
			if(_Operator.getRawButton(2)){
				//GamePiece Presets Middle (Hatch)
				if(_Operator.getRawButton(7)){
					//GamePiece Presets High (Cargo)
        }
      }
      
			//if(_Operator.getRawButton(4)){
				//GamePiece Presets High (Hatch)
				//if(_Operator.getRawButton(7)){
					//GamePiece Presets High (Cargo)
        //}
      //}
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
