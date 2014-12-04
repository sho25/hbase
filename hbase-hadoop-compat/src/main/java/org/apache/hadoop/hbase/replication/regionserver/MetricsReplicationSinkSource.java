begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|replication
operator|.
name|regionserver
package|;
end_package

begin_interface
specifier|public
interface|interface
name|MetricsReplicationSinkSource
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SINK_AGE_OF_LAST_APPLIED_OP
init|=
literal|"sink.ageOfLastAppliedOp"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SINK_APPLIED_BATCHES
init|=
literal|"sink.appliedBatches"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SINK_APPLIED_OPS
init|=
literal|"sink.appliedOps"
decl_stmt|;
name|void
name|setLastAppliedOpAge
parameter_list|(
name|long
name|age
parameter_list|)
function_decl|;
name|void
name|incrAppliedBatches
parameter_list|(
name|long
name|batches
parameter_list|)
function_decl|;
name|void
name|incrAppliedOps
parameter_list|(
name|long
name|batchsize
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

