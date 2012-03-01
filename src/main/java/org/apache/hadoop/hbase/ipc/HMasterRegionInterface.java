begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HServerLoad
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
name|ServerName
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
name|security
operator|.
name|KerberosInfo
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
name|io
operator|.
name|MapWritable
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
name|ipc
operator|.
name|VersionedProtocol
import|;
end_import

begin_comment
comment|/**  * The Master publishes this Interface for RegionServers to register themselves  * on.  */
end_comment

begin_interface
annotation|@
name|KerberosInfo
argument_list|(
name|serverPrincipal
operator|=
literal|"hbase.master.kerberos.principal"
argument_list|,
name|clientPrincipal
operator|=
literal|"hbase.regionserver.kerberos.principal"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HMasterRegionInterface
extends|extends
name|VersionedProtocol
block|{
comment|/**    * This Interfaces' version. Version changes when the Interface changes.    */
comment|// All HBase Interfaces used derive from HBaseRPCProtocolVersion.  It
comment|// maintained a single global version number on all HBase Interfaces.  This
comment|// meant all HBase RPC was broke though only one of the three RPC Interfaces
comment|// had changed.  This has since been undone.
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|29L
decl_stmt|;
comment|/**    * Called when a region server first starts.    * @param port Port number this regionserver is up on.    * @param serverStartcode This servers' startcode.    * @param serverCurrentTime The current time of the region server in ms    * @throws IOException e    * @return Configuration for the regionserver to use: e.g. filesystem,    * hbase rootdir, the hostname to use creating the RegionServer ServerName,    * etc.    */
specifier|public
name|MapWritable
name|regionServerStartup
parameter_list|(
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|long
name|serverStartcode
parameter_list|,
specifier|final
name|long
name|serverCurrentTime
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param sn {@link ServerName#getVersionedBytes()}    * @param hsl Server load.    * @throws IOException    */
specifier|public
name|void
name|regionServerReport
parameter_list|(
name|byte
index|[]
name|sn
parameter_list|,
name|HServerLoad
name|hsl
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called by a region server to report a fatal error that is causing    * it to abort.    * @param sn {@link ServerName#getVersionedBytes()}    * @param errorMessage informative text to expose in the master logs and UI    */
specifier|public
name|void
name|reportRSFatalError
parameter_list|(
name|byte
index|[]
name|sn
parameter_list|,
name|String
name|errorMessage
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

