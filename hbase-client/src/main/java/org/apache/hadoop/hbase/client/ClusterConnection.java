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
name|MasterNotRunningException
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
name|TableName
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
name|ZooKeeperConnectionException
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
name|backoff
operator|.
name|ClientBackoffPolicy
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
name|RpcControllerFactory
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|AdminService
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
import|;
end_import

begin_comment
comment|/** Internal methods on Connection that should not be used by user code. */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|// NOTE: Although this class is public, this class is meant to be used directly from internal
comment|// classes and unit tests only.
specifier|public
interface|interface
name|ClusterConnection
extends|extends
name|Connection
block|{
comment|/**    * Key for configuration in Configuration whose value is the class we implement making a    * new Connection instance.    */
name|String
name|HBASE_CLIENT_CONNECTION_IMPL
init|=
literal|"hbase.client.connection.impl"
decl_stmt|;
comment|/**    * @return - true if the master server is running    * @deprecated this has been deprecated without a replacement    */
annotation|@
name|Deprecated
name|boolean
name|isMasterRunning
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
function_decl|;
comment|/**    * Use this api to check if the table has been created with the specified number of    * splitkeys which was used while creating the given table.    * Note : If this api is used after a table's region gets splitted, the api may return    * false.    * @param tableName    *          tableName    * @param splitKeys    *          splitKeys used while creating table    * @throws IOException    *           if a remote or network exception occurs    */
name|boolean
name|isTableAvailable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * A table that isTableEnabled == false and isTableDisabled == false    * is possible. This happens when a table has a lot of regions    * that must be processed.    * @param tableName table name    * @return true if the table is enabled, false otherwise    * @throws IOException if a remote or network exception occurs    */
name|boolean
name|isTableEnabled
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param tableName table name    * @return true if the table is disabled, false otherwise    * @throws IOException if a remote or network exception occurs    */
name|boolean
name|isTableDisabled
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve TableState, represent current table state.    * @param tableName table state for    * @return state of the table    */
name|TableState
name|getTableState
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns a {@link MasterKeepAliveConnection} to the active master    */
name|MasterKeepAliveConnection
name|getMaster
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the admin service for master.    */
name|AdminService
operator|.
name|BlockingInterface
name|getAdminForMaster
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address.    * @param serverName the region server to connect to    * @return proxy for HRegionServer    * @throws IOException if a remote or network exception occurs    */
name|AdminService
operator|.
name|BlockingInterface
name|getAdmin
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Establishes a connection to the region server at the specified address, and returns    * a region client protocol.    *    * @param serverName the region server to connect to    * @return ClientProtocol proxy for RegionServer    * @throws IOException if a remote or network exception occurs    *    */
name|ClientService
operator|.
name|BlockingInterface
name|getClient
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return Nonce generator for this ClusterConnection; may be null if disabled in configuration.    */
name|NonceGenerator
name|getNonceGenerator
parameter_list|()
function_decl|;
comment|/**    * @return Default AsyncProcess associated with this connection.    */
name|AsyncProcess
name|getAsyncProcess
parameter_list|()
function_decl|;
comment|/**    * Returns a new RpcRetryingCallerFactory from the given {@link Configuration}.    * This RpcRetryingCallerFactory lets the users create {@link RpcRetryingCaller}s which can be    * intercepted with the configured {@link RetryingCallerInterceptor}    * @param conf configuration    * @return RpcRetryingCallerFactory    */
name|RpcRetryingCallerFactory
name|getNewRpcRetryingCallerFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * @return Connection's RpcRetryingCallerFactory instance    */
name|RpcRetryingCallerFactory
name|getRpcRetryingCallerFactory
parameter_list|()
function_decl|;
comment|/**    * @return Connection's RpcControllerFactory instance    */
name|RpcControllerFactory
name|getRpcControllerFactory
parameter_list|()
function_decl|;
comment|/**    * @return a ConnectionConfiguration object holding parsed configuration values    */
name|ConnectionConfiguration
name|getConnectionConfiguration
parameter_list|()
function_decl|;
comment|/**    * @return the current statistics tracker associated with this connection    */
name|ServerStatisticTracker
name|getStatisticsTracker
parameter_list|()
function_decl|;
comment|/**    * @return the configured client backoff policy    */
name|ClientBackoffPolicy
name|getBackoffPolicy
parameter_list|()
function_decl|;
comment|/**    * @return the MetricsConnection instance associated with this connection.    */
name|MetricsConnection
name|getConnectionMetrics
parameter_list|()
function_decl|;
comment|/**    * @return true when this connection uses a {@link org.apache.hadoop.hbase.codec.Codec} and so    *         supports cell blocks.    */
name|boolean
name|hasCellBlockSupport
parameter_list|()
function_decl|;
comment|/**    * @return the number of region servers that are currently running    * @throws IOException if a remote or network exception occurs    */
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

