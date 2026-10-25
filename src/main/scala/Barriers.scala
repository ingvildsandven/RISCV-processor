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
            val flush           = Input(Bool())
            val stall           = Input(Bool())
        }
    )

    // Defining a register where PC is stored
    val PCDelayed        = RegInit(0.U(32.W))
    val savedInstruction = RegInit(Instruction.NOP)
    
    // Remember whether the previous cycle was stalled
    val wasStalled       = RegNext(io.stall, false.B)
    val flushDelayed     = RegNext(io.flush, false.B)

    // Capture the instruction when a stall first begins
    when(io.stall && !wasStalled) {
        savedInstruction  := io.instructionIn
    }

    when(!io.stall){
        PCDelayed         := io.PCIn
    }

    when(io.flush || flushDelayed) {
        io.instructionOut := Instruction.NOP
    }.elsewhen(wasStalled) {
        io.instructionOut := savedInstruction
    }.otherwise {
        io.instructionOut := io.instructionIn
    }

    io.PCOut          := PCDelayed
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
            val registerRs1In     = Input(UInt(5.W))
            val registerRs2In     = Input(UInt(5.W))
            val flush             = Input(Bool())
            val stall             = Input(Bool())

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
            val registerRs1Out    = Output(UInt(5.W))
            val registerRs2Out    = Output(UInt(5.W))

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
    val registerRs1Delayed    = RegInit(0.U(5.W))
    val registerRs2Delayed    = RegInit(0.U(5.W))

    when(!io.stall){
    // Values to each register
    PCDelayed                := io.PCIn
    branchTypeOutDelayed     := io.branchTypeIn
    op1SelectDelayed         := io.op1SelectIn
    op2SelectDelayed         := io.op2SelectIn
    immediateDelayed         := io.immediateIn
    ALUopDelayed             := io.ALUopIn
    readData1Delayed         := io.readData1In
    readData2Delayed         := io.readData2In
    registerRdOutDelayed     := io.registerRdIn
    registerRs1Delayed       := io.registerRs1In
    registerRs2Delayed       := io.registerRs2In

    when(io.flush){
        controlSignalsDelayed := ControlSignals.nop
    }.otherwise{
        controlSignalsDelayed := io.controlSignalsIn
    }
    }

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
    io.registerRs1Out        := registerRs1Delayed
    io.registerRs2Out        := registerRs2Delayed
    
}

class EXMEMBarrier extends MultiIOModule { // AI generated

  val io = IO(
    new Bundle {
      // Inputs
      val PCIn                = Input(UInt(32.W))
      val aluResultIn         = Input(UInt(32.W))
      val readData2In         = Input(UInt(32.W))
      val registerRdIn        = Input(UInt(5.W))
      val controlSignalsIn    = Input(new ControlSignals)
      val stall               = Input(Bool())


      // Outputs
      val PCOut               = Output(UInt(32.W))
      val aluResultOut        = Output(UInt(32.W))
      val readData2Out        = Output(UInt(32.W))
      val registerRdOut       = Output(UInt(5.W))
      val controlSignalsOut   = Output(new ControlSignals)
    }
  )

  // Registers
  val PCDelayed              = RegInit(0.U(32.W))
  val aluResultDelayed       = RegInit(0.U(32.W))
  val readData2Delayed       = RegInit(0.U(32.W))
  val registerRdDelayed      = RegInit(0.U(5.W))
  val controlSignalsDelayed  = RegInit(0.U.asTypeOf(new ControlSignals))

  // Store EX outputs for one cycle
  PCDelayed             := io.PCIn
  aluResultDelayed      := io.aluResultIn
  readData2Delayed      := io.readData2In
  registerRdDelayed     := io.registerRdIn
 
  // Makes controlsignal bubble on stall but keep executing
  when(io.stall) {
    controlSignalsDelayed := ControlSignals.nop
  }.otherwise {
    controlSignalsDelayed := io.controlSignalsIn
  }

  // Outputs
  io.PCOut              := PCDelayed
  io.aluResultOut       := aluResultDelayed
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