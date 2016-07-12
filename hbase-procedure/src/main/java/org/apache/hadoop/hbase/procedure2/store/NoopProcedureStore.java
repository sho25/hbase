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
name|procedure2
operator|.
name|store
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|hbase
operator|.
name|procedure2
operator|.
name|Procedure
import|;
end_import

begin_comment
comment|/**  * An In-Memory store that does not keep track of the procedures inserted.  */
end_comment

begin_class
specifier|public
class|class
name|NoopProcedureStore
extends|extends
name|ProcedureStoreBase
block|{
specifier|private
name|int
name|numThreads
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|setRunning
argument_list|(
literal|true
argument_list|)
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|numThreads
operator|=
name|numThreads
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|boolean
name|abort
parameter_list|)
block|{
name|setRunning
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|recoverLease
parameter_list|()
throws|throws
name|IOException
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumThreads
parameter_list|()
block|{
return|return
name|numThreads
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|load
parameter_list|(
specifier|final
name|ProcedureLoader
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
name|loader
operator|.
name|setMaxProcId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|insert
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|Procedure
index|[]
name|subprocs
parameter_list|)
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|Procedure
name|proc
parameter_list|)
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|long
name|procId
parameter_list|)
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|long
index|[]
name|subprocs
parameter_list|)
block|{
comment|// no-op
block|}
block|}
end_class

end_unit

