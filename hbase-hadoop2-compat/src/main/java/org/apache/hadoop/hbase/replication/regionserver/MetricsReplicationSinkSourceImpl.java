begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|metrics2
operator|.
name|lib
operator|.
name|MutableFastCounter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|metrics2
operator|.
name|lib
operator|.
name|MutableGaugeLong
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|metrics2
operator|.
name|lib
operator|.
name|MutableHistogram
import|;
end_import

begin_class
specifier|public
class|class
name|MetricsReplicationSinkSourceImpl
implements|implements
name|MetricsReplicationSinkSource
block|{
specifier|private
specifier|final
name|MutableHistogram
name|ageHist
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|batchesCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|opsCounter
decl_stmt|;
specifier|private
specifier|final
name|MutableFastCounter
name|hfilesCounter
decl_stmt|;
specifier|public
name|MetricsReplicationSinkSourceImpl
parameter_list|(
name|MetricsReplicationSourceImpl
name|rms
parameter_list|)
block|{
name|ageHist
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getHistogram
argument_list|(
name|SINK_AGE_OF_LAST_APPLIED_OP
argument_list|)
expr_stmt|;
name|batchesCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getCounter
argument_list|(
name|SINK_APPLIED_BATCHES
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|opsCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getCounter
argument_list|(
name|SINK_APPLIED_OPS
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|hfilesCounter
operator|=
name|rms
operator|.
name|getMetricsRegistry
argument_list|()
operator|.
name|getCounter
argument_list|(
name|SINK_APPLIED_HFILES
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setLastAppliedOpAge
parameter_list|(
name|long
name|age
parameter_list|)
block|{
name|ageHist
operator|.
name|add
argument_list|(
name|age
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrAppliedBatches
parameter_list|(
name|long
name|batches
parameter_list|)
block|{
name|batchesCounter
operator|.
name|incr
argument_list|(
name|batches
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrAppliedOps
parameter_list|(
name|long
name|batchsize
parameter_list|)
block|{
name|opsCounter
operator|.
name|incr
argument_list|(
name|batchsize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastAppliedOpAge
parameter_list|()
block|{
return|return
name|ageHist
operator|.
name|getMax
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrAppliedHFiles
parameter_list|(
name|long
name|hfiles
parameter_list|)
block|{
name|hfilesCounter
operator|.
name|incr
argument_list|(
name|hfiles
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

