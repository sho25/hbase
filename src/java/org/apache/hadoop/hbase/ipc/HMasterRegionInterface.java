begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ipc
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
name|io
operator|.
name|HbaseMapWritable
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
name|ipc
operator|.
name|VersionedProtocol
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
name|HServerInfo
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
name|HMsg
import|;
end_import

begin_comment
comment|/**  * HRegionServers interact with the HMasterRegionInterface to report on local   * goings-on and to obtain data-handling instructions from the HMaster.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HMasterRegionInterface
extends|extends
name|VersionedProtocol
block|{
comment|/** Interface version number */
specifier|public
specifier|static
specifier|final
name|long
name|versionID
init|=
literal|1L
decl_stmt|;
comment|/**    * Called when a region server first starts    * @param info    * @throws IOException    * @return Configuration for the regionserver to use: e.g. filesystem,    * hbase rootdir, etc.    */
specifier|public
name|HbaseMapWritable
name|regionServerStartup
parameter_list|(
name|HServerInfo
name|info
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called to renew lease, tell master what the region server is doing and to    * receive new instructions from the master    *     * @param info server's address and start code    * @param msgs things the region server wants to tell the master    * @return instructions from the master to the region server    * @throws IOException    */
specifier|public
name|HMsg
index|[]
name|regionServerReport
parameter_list|(
name|HServerInfo
name|info
parameter_list|,
name|HMsg
name|msgs
index|[]
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

