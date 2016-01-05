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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Simple Service.  */
end_comment

begin_comment
comment|// This is a WIP. We have Services throughout hbase. Either have all implement what is here or
end_comment

begin_comment
comment|// just remove this as an experiment that did not work out.
end_comment

begin_comment
comment|// TODO: Move on to guava Service after we update our guava version; later guava has nicer
end_comment

begin_comment
comment|// Service implmentation.
end_comment

begin_comment
comment|// TODO: Move all Services on to this one Interface.
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Service
block|{
comment|/**    * Initiates service startup (if necessary), returning once the service has finished starting.    * @throws IOException Throws exception if already running and if we fail to start successfully.    */
name|void
name|startAndWait
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return True if this Service is running.    */
name|boolean
name|isRunning
parameter_list|()
function_decl|;
comment|/**    * Initiates service shutdown (if necessary), returning once the service has finished stopping.    * @throws IOException Throws exception if not running of if we fail to stop successfully.    */
name|void
name|stopAndWait
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

