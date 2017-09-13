begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Interface to support the aborting of a given server or client.  *<p>  * This is used primarily for ZooKeeper usage when we could get an unexpected  * and fatal exception, requiring an abort.  *<p>  * Implemented by the Master, RegionServer, and TableServers (client).  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|Abortable
block|{
comment|/**    * Abort the server or client.    * @param why Why we're aborting.    * @param e Throwable that caused abort. Can be null.    */
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
function_decl|;
comment|/**    * Check if the server or client was aborted.    * @return true if the server or client was aborted, false otherwise    */
name|boolean
name|isAborted
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

