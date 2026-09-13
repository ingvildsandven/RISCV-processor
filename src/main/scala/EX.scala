package FiveStage

import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class Execution extends MultiIOModule {


  val io = IO(
    new Bundle {
      val PC             = Input(UInt(32.W))
      val readData1      = Input(UInt(32.W))
      val readData2      = Input(UInt(32.W))
      val immediate      = Input(SInt(32.W))
      val registerRd     = Input(UInt(5.W))

      val controlSignals = Input(new ControlSignals)
      val branchType     = Input(UInt(3.W))
      val op1Select      = Input(UInt(1.W))
      val op2Select      = Input(UInt(1.W))
      val ALUop          = Input(UInt(4.W))

      val aluResult         = Output(UInt(32.W))
      val branchTaken       = Output(Bool())
      val branchTarget      = Output(UInt(32.W))
      val readData2Out      = Output(UInt(32.W))
      val registerRdOut     = Output(UInt(5.W))
      val controlSignalsOut = Output(new ControlSignals)
    }
  )
  val alu     = Module(new ALU).io
  val branch  = Module(new BranchUnit).io

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
      alu.op1 := io.PC
    }
    is(Op1Select.rs1){
      alu.op1 := io.readData1
    }
  }

  switch(io.op2Select){
    is(Op2Select.rs2){
      alu.op2 := io.readData2
    }
    is(Op2Select.imm){
      alu.op2 := io.immediate.asUInt
    }
  }

  // =====================
  //   Branch logic
  // =====================
  branch.r1         := io.readData1
  branch.r2         := io.readData2
  branch.branchType := io.branchType

  io.branchTaken := (io.controlSignals.branch || io.controlSignals.jump) && branch.branchTaken
  io.branchTarget := io.PC + io.immediate.asUInt

  // Sets the output 
  io.aluResult  := alu.aluResult
  io.readData2Out      := io.readData2
  io.registerRdOut     := io.registerRd
  io.controlSignalsOut := io.controlSignals
  
}
