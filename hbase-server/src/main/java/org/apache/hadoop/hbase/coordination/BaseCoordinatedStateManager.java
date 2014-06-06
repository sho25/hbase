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
name|coordination
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
name|classification
operator|.
name|InterfaceAudience
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
name|CoordinatedStateException
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
name|CoordinatedStateManager
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
name|Server
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
name|TableStateManager
import|;
end_import

begin_comment
comment|/**  * Base class for {@link org.apache.hadoop.hbase.CoordinatedStateManager} implementations.  * Defines methods to retrieve coordination objects for relevant areas. CoordinatedStateManager  * reference returned from Server interface has to be casted to this type to  * access those methods.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|BaseCoordinatedStateManager
implements|implements
name|CoordinatedStateManager
block|{
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Server
name|server
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|Server
name|getServer
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|TableStateManager
name|getTableStateManager
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|CoordinatedStateException
function_decl|;
comment|/**    * Method to retrieve coordination for split transaction.    */
specifier|abstract
specifier|public
name|SplitTransactionCoordination
name|getSplitTransactionCoordination
parameter_list|()
function_decl|;
comment|/**    * Method to retrieve coordination for closing region operations.    */
specifier|public
specifier|abstract
name|CloseRegionCoordination
name|getCloseRegionCoordination
parameter_list|()
function_decl|;
comment|/**    * Method to retrieve coordination for opening region operations.    */
specifier|public
specifier|abstract
name|OpenRegionCoordination
name|getOpenRegionCoordination
parameter_list|()
function_decl|;
block|}
end_class

end_unit

