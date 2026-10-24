package FiveStage

import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class Execution extends MultiIOModule {


  val io = IO(
    new Bundle {
      val PCIn           = Input(UInt(32.W))
      val readData1      = Input(UInt(32.W))
      val readData2      = Input(UInt(32.W))
      val immediate      = Input(SInt(32.W))
      val registerRd     = Input(UInt(5.W))
      val registerRs1    = Input(UInt(5.W))
      val registerRs2    = Input(UInt(5.W))

      val controlSignals = Input(new ControlSignals)
      val branchType     = Input(UInt(3.W))
      val op1Select      = Input(UInt(1.W))
      val op2Select      = Input(UInt(1.W))
      val ALUop          = Input(UInt(4.W))

      // Instructions in MEM
      val memRd          = Input(UInt(5.W))
      val memRegWrite    = Input(Bool())
      val memRead        = Input(Bool())
      val memResult      = Input(UInt(32.W))

      // Instruction in WB
      val wbRd       = Input(UInt(5.W))
      val wbRegWrite = Input(Bool())
      val wbResult   = Input(UInt(32.W))

      // Output
      val PCOut             = Output(UInt(32.W))
      val aluResult         = Output(UInt(32.W))
      val branchTaken       = Output(Bool())
      val branchTarget      = Output(UInt(32.W))
      val readData2Out      = Output(UInt(32.W))
      val registerRdOut     = Output(UInt(5.W))
      val controlSignalsOut = Output(new ControlSignals)
      val stall             = Output(Bool())
    }
  )
  val forward = Module(new ForwardingUnit).io
  val alu     = Module(new ALU).io
  val branch  = Module(new BranchUnit).io

  // =====================
  //   Forwarding logic
  // =====================
  forward.rs1 := io.registerRs1
  forward.rs2 := io.registerRs2

  forward.registerVal1 := io.readData1
  forward.registerVal2 := io.readData2

  forward.memRd       := io.memRd
  forward.memRegWrite := io.memRegWrite
  forward.memRead     := io.memRead
  forward.memResult   := io.memResult

  forward.wbRd       := io.wbRd
  forward.wbRegWrite := io.wbRegWrite
  forward.wbResult   := io.wbResult

  forward.usesRs1 := (io.op1Select === Op1Select.rs1) || io.controlSignals.branch
  forward.usesRs2 := (io.op2Select === Op2Select.rs2) || io.controlSignals.branch || io.controlSignals.memWrite

  // =====================
  //   ALU logic
  // =====================
  
  alu.aluOp     := io.ALUop
  
  // default
  alu.op1 := 0.U
  alu.op2 := 0.U

  // these are the muxes setting the ALU input 
  switch(io.op1Select){
    is(Op1Select.PC){
      alu.op1 := io.PCIn
    }
    is(Op1Select.rs1){
      alu.op1 := forward.forwardedRs1
    }
  }

  switch(io.op2Select){
    is(Op2Select.rs2){
      alu.op2 := forward.forwardedRs2
    }
    is(Op2Select.imm){
      alu.op2 := io.immediate.asUInt
    }
  }

  // =====================
  //   Branch logic
  // =====================
  branch.r1         := forward.forwardedRs1
  branch.r2         := forward.forwardedRs2
  branch.branchType := io.branchType

  io.branchTaken    := (io.controlSignals.branch || io.controlSignals.jump) && branch.branchTaken

 when(io.controlSignals.jump) {
    when(io.op1Select === Op1Select.rs1) {// JALR
      io.branchTarget := alu.aluResult & "hFFFFFFFE".U
    }.otherwise {// JAL 
      io.branchTarget := alu.aluResult
    }
  }.otherwise {// BEQ/BNE/BLT...
    io.branchTarget := io.PCIn + io.immediate.asUInt
  }

  // Sets the output 
  io.PCOut             := io.PCIn
  io.aluResult         := alu.aluResult
  io.readData2Out      := forward.forwardedRs2
  io.registerRdOut     := io.registerRd
  io.controlSignalsOut := io.controlSignals
  io.stall             := forward.stall
  
}
