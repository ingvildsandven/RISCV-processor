
package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule


class ForwardingUnit extends MultiIOModule {

  val io = IO(new Bundle {
    // Source registers of the instruction in EX
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val registerVal1 = Input(UInt(32.W))
    val registerVal2 = Input(UInt(32.W))
    val usesRs1 = Input(Bool())
    val usesRs2 = Input(Bool())

    // Instruction currently in MEM
    val memRd = Input(UInt(5.W))
    val memRegWrite = Input(Bool())
    val memRead = Input(Bool())
    val memResult = Input(UInt(32.W))

    // Instruction currently in WB
    val wbRd = Input(UInt(5.W))
    val wbRegWrite = Input(Bool())
    val wbResult = Input(UInt(32.W))

    // Selected register values, before ALU operand selection
    val forwardedRs1 = Output(UInt(32.W))
    val forwardedRs2 = Output(UInt(32.W))
    val stall = Output(Bool())
  })

  // Check if reg address match mem
  val rs1matchesMEM = (io.rs1 === io.memRd) &&        // Checks if the dest address is the same as in memory
                       io.memRegWrite &&              // Checks that the instruction writes to register
                       (io.rs1 =/= 0.U) &&            // Ensures value is discarded if dest is x0
                       io.usesRs1                     // Checks that EXEC uses rs1
  val rs2matchesMEM = (io.rs2 === io.memRd) &&        
                       io.memRegWrite &&        
                       (io.rs2 =/= 0.U) &&        
                       io.usesRs2                     


  // Check if reg address match wb
  val rs1matchesWB = (io.rs1 === io.wbRd) &&   
                       io.wbRegWrite &&     
                       (io.rs1 =/= 0.U) &&      
                       io.usesRs1                 
  val rs2matchesWB = (io.rs2 === io.wbRd) &&        
                       io.wbRegWrite &&        
                       (io.rs2 =/= 0.U) &&        
                       io.usesRs2     

  io.stall := false.B
  io.forwardedRs1 := io.registerVal1
  io.forwardedRs2 := io.registerVal2

  // Forward
  when(rs1matchesMEM){
    when(io.memRead){ // A matching load has not produced its value yet (for lw before add)
        io.stall := true.B
    }.otherwise{
        io.forwardedRs1 := io.memResult
    }

  }.elsewhen(rs1matchesWB){
        io.forwardedRs1 := io.wbResult
  }.otherwise{
    io.forwardedRs1 := io.registerVal1
  }

  when(rs2matchesMEM){
    when(io.memRead){ // A matching load has not produced its value yet
        io.stall := true.B
    }.otherwise{
        io.forwardedRs2 := io.memResult
    }

  }.elsewhen(rs2matchesWB){
    io.forwardedRs2 := io.wbResult
  }.otherwise{
    io.forwardedRs2 := io.registerVal2
  }

  
}
