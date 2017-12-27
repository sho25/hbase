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
name|regionserver
operator|.
name|handler
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
name|hbase
operator|.
name|executor
operator|.
name|EventHandler
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
name|RSProcedureCallable
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
name|regionserver
operator|.
name|HRegionServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A event handler for running procedure.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSProcedureHandler
extends|extends
name|EventHandler
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|RSProcedureHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|long
name|procId
decl_stmt|;
specifier|private
specifier|final
name|RSProcedureCallable
name|callable
decl_stmt|;
specifier|public
name|RSProcedureHandler
parameter_list|(
name|HRegionServer
name|rs
parameter_list|,
name|long
name|procId
parameter_list|,
name|RSProcedureCallable
name|callable
parameter_list|)
block|{
name|super
argument_list|(
name|rs
argument_list|,
name|callable
operator|.
name|getEventType
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|procId
operator|=
name|procId
expr_stmt|;
name|this
operator|.
name|callable
operator|=
name|callable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
name|Exception
name|error
init|=
literal|null
decl_stmt|;
try|try
block|{
name|callable
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Catch exception when call RSProcedureCallable: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|error
operator|=
name|e
expr_stmt|;
block|}
operator|(
operator|(
name|HRegionServer
operator|)
name|server
operator|)
operator|.
name|remoteProcedureComplete
argument_list|(
name|procId
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

