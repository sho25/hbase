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
name|client
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
name|InterfaceStability
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
name|conf
operator|.
name|Configuration
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
name|CoprocessorEnvironment
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
name|client
operator|.
name|ConnectionManager
operator|.
name|HConnectionImplementation
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|RegionServerServices
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
name|UserProvider
import|;
end_import

begin_comment
comment|/**  * Connection to an HTable from within a Coprocessor. We can do some nice tricks since we know we  * are on a regionserver, for instance skipping the full serialization/deserialization of objects  * when talking to the server.  *<p>  * You should not use this class from any client - its an internal class meant for use by the  * coprocessor framework.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CoprocessorHConnection
extends|extends
name|HConnectionImplementation
block|{
specifier|private
specifier|static
specifier|final
name|NonceGenerator
name|NO_NONCE_GEN
init|=
operator|new
name|ConnectionManager
operator|.
name|NoNonceGenerator
argument_list|()
decl_stmt|;
comment|/**    * Create an unmanaged {@link HConnection} based on the environment in which we are running the    * coprocessor. The {@link HConnection} must be externally cleaned up (we bypass the usual HTable    * cleanup mechanisms since we own everything).    * @param env environment hosting the {@link HConnection}    * @return an unmanaged {@link HConnection}.    * @throws IOException if we cannot create the connection    */
specifier|public
specifier|static
name|ClusterConnection
name|getConnectionForEnvironment
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// this bit is a little hacky - just trying to get it going for the moment
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|RegionCoprocessorEnvironment
name|e
init|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
decl_stmt|;
name|RegionServerServices
name|services
init|=
name|e
operator|.
name|getRegionServerServices
argument_list|()
decl_stmt|;
if|if
condition|(
name|services
operator|instanceof
name|HRegionServer
condition|)
block|{
return|return
operator|new
name|CoprocessorHConnection
argument_list|(
operator|(
name|HRegionServer
operator|)
name|services
argument_list|)
return|;
block|}
block|}
return|return
name|ConnectionManager
operator|.
name|createConnectionInternal
argument_list|(
name|env
operator|.
name|getConfiguration
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
comment|/**    * Legacy constructor    * @param delegate    * @param server    * @throws IOException if we cannot create the connection    * @deprecated delegate is not used    */
annotation|@
name|Deprecated
specifier|public
name|CoprocessorHConnection
parameter_list|(
name|ClusterConnection
name|delegate
parameter_list|,
name|HRegionServer
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor that uses server configuration    * @param server    * @throws IOException if we cannot create the connection    */
specifier|public
name|CoprocessorHConnection
parameter_list|(
name|HRegionServer
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|server
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|server
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor that accepts custom configuration    * @param conf    * @param server    * @throws IOException if we cannot create the connection    */
specifier|public
name|CoprocessorHConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegionServer
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
operator|.
name|getCurrent
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|server
operator|.
name|getServerName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|getClient
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// client is trying to reach off-server, so we can't do anything special
if|if
condition|(
operator|!
name|this
operator|.
name|serverName
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
return|return
name|super
operator|.
name|getClient
argument_list|(
name|serverName
argument_list|)
return|;
block|}
comment|// the client is attempting to write to the same regionserver, we can short-circuit to our
comment|// local regionserver
return|return
name|server
operator|.
name|getRSRpcServices
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
block|{
return|return
name|NO_NONCE_GEN
return|;
comment|// don't use nonces for coprocessor connection
block|}
block|}
end_class

end_unit

