package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import javax.swing.InputMap

class IFIDBarrier extends MultiIOModule{

    val io = IO(
        new Bundle{
            val instructionIn   = Input(new Instruction)
            val instructionOut  = Output(new Instruction)
            val PCIn            = Input(UInt(32.W))
            val PCOut           = Output(UInt(32.W))
        }
    )

    // Defining a register where PC is stored
    val PCDelayed      = RegInit(0.U(32.W))

    PCDelayed         := io.PCIn
    io.PCOut          := PCDelayed

    io.instructionOut := io.instructionIn
}

class IDEXBarrier extends MultiIOModule{

    val io = IO(
        new Bundle{
            // Inputs
            val PCIn              = Input(UInt(32.W))
            val controlSignalsIn  = Input(new ControlSignals)
            val branchTypeIn      = Input(UInt(3.W))
            val op1SelectIn       = Input(UInt(1.W))
            val op2SelectIn       = Input(UInt(1.W))
            val immediateIn       = Input(SInt(32.W))
            val ALUopIn           = Input(UInt(4.W))

            val readData1In       = Input(UInt(32.W))
            val readData2In       = Input(UInt(32.W))
            val registerRdIn      = Input(UInt(5.W))

            // Output
            val PCOut             = Output(UInt(32.W))
            val controlSignalsOut = Output(new ControlSignals)
            val branchTypeOut     = Output(UInt(3.W))
            val op1SelectOut      = Output(UInt(1.W))
            val op2SelectOut      = Output(UInt(1.W))
            val immediateOut      = Output(SInt(32.W))
            val ALUopOut          = Output(UInt(4.W))

            val readData1Out      = Output(UInt(32.W))
            val readData2Out      = Output(UInt(32.W))
            val registerRdOut     = Output(UInt(5.W))
        }
    )

    // Defining registers where these can be stored
    val PCDelayed             = RegInit(0.U(32.W))
    val controlSignalsDelayed = RegInit(0.U.asTypeOf(new ControlSignals))
    val branchTypeOutDelayed  = RegInit(0.U(3.W))
    val op1SelectDelayed      = RegInit(0.U(1.W))
    val op2SelectDelayed      = RegInit(0.U(1.W))
    val immediateDelayed      = RegInit(0.S(32.W))
    val ALUopDelayed          = RegInit(0.U(4.W))
    val readData1Delayed      = RegInit(0.U(32.W))
    val readData2Delayed      = RegInit(0.U(32.W))
    val registerRdOutDelayed  = RegInit(0.U(5.W))

    // Values to each register
    PCDelayed                := io.PCIn
    controlSignalsDelayed    := io.controlSignalsIn
    branchTypeOutDelayed     := io.branchTypeIn
    op1SelectDelayed         := io.op1SelectIn
    op2SelectDelayed         := io.op2SelectIn
    immediateDelayed         := io.immediateIn
    ALUopDelayed             := io.ALUopIn
    readData1Delayed         := io.readData1In
    readData2Delayed         := io.readData2In
    registerRdOutDelayed     := io.registerRdIn

    // Set output
    io.PCOut                 := PCDelayed
    io.controlSignalsOut     := controlSignalsDelayed   
    io.branchTypeOut         := branchTypeOutDelayed  
    io.op1SelectOut          := op1SelectDelayed 
    io.op2SelectOut          := op2SelectDelayed 
    io.immediateOut          := immediateDelayed   
    io.ALUopOut              := ALUopDelayed        
    io.readData1Out          := readData1Delayed    
    io.readData2Out          := readData2Delayed        
    io.registerRdOut         := registerRdOutDelayed     
    
}

class EXMEMBarrier extends MultiIOModule { // AI generated

  val io = IO(
    new Bundle {
      // Inputs
      val aluResultIn         = Input(UInt(32.W))
      val branchTakenIn       = Input(Bool())
      val branchTargetIn      = Input(UInt(32.W))
      val readData2In         = Input(UInt(32.W))
      val registerRdIn        = Input(UInt(5.W))
      val controlSignalsIn    = Input(new ControlSignals)

      // Outputs
      val aluResultOut        = Output(UInt(32.W))
      val branchTakenOut      = Output(Bool())
      val branchTargetOut     = Output(UInt(32.W))
      val readData2Out        = Output(UInt(32.W))
      val registerRdOut       = Output(UInt(5.W))
      val controlSignalsOut   = Output(new ControlSignals)
    }
  )

  // Registers
  val aluResultDelayed       = RegInit(0.U(32.W))
  val branchTakenDelayed     = RegInit(false.B)
  val branchTargetDelayed    = RegInit(0.U(32.W))
  val readData2Delayed       = RegInit(0.U(32.W))
  val registerRdDelayed      = RegInit(0.U(5.W))
  val controlSignalsDelayed  = RegInit(0.U.asTypeOf(new ControlSignals))

  // Store EX outputs for one cycle
  aluResultDelayed      := io.aluResultIn
  branchTakenDelayed    := io.branchTakenIn
  branchTargetDelayed   := io.branchTargetIn
  readData2Delayed      := io.readData2In
  registerRdDelayed     := io.registerRdIn
  controlSignalsDelayed := io.controlSignalsIn

  // Outputs
  io.aluResultOut       := aluResultDelayed
  io.branchTakenOut     := branchTakenDelayed
  io.branchTargetOut    := branchTargetDelayed
  io.readData2Out       := readData2Delayed
  io.registerRdOut      := registerRdDelayed
  io.controlSignalsOut  := controlSignalsDelayed
}

class MEMWBBarrier extends MultiIOModule { 

  val io = IO(
    new Bundle {
        val memoryDataIn        = Input(UInt(32.W))
        val aluResultIn         = Input(UInt(32.W))
        val controlSignalsIn    = Input(new ControlSignals)
        val registerRdIn        = Input(UInt(5.W))

        val memoryDataOut       = Output(UInt(32.W))
        val aluResultOut        = Output(UInt(32.W))
        val controlSignalsOut   = Output(new ControlSignals)
        val registerRdOut       = Output(UInt(5.W))
    })

    val aluResultDelayed       = RegInit(0.U(32.W))
    val controlSignalsDelayed  = RegInit(0.U.asTypeOf(new ControlSignals))
    val registerRdDelayed      = RegInit(0.U(5.W))

    aluResultDelayed          := io.aluResultIn
    controlSignalsDelayed     := io.controlSignalsIn
    registerRdDelayed         := io.registerRdIn

    io.memoryDataOut          := io.memoryDataIn // Is delayed in the Dmem already
    io.aluResultOut           := aluResultDelayed
    io.controlSignalsOut      := controlSignalsDelayed
    io.registerRdOut          := registerRdDelayed

}