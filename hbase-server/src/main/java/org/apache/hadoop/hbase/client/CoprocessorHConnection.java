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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
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
name|HRegionLocation
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
name|HTableDescriptor
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
name|HConnection
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
name|HConnectionManager
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
name|HTableInterface
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
name|Row
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
name|coprocessor
operator|.
name|Batch
operator|.
name|Callback
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
name|ipc
operator|.
name|RpcServerInterface
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
name|monitoring
operator|.
name|MonitoredRPCHandler
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
name|monitoring
operator|.
name|TaskMonitor
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
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
name|util
operator|.
name|EnvironmentEdgeManager
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingRpcChannel
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingService
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
implements|implements
name|HConnection
block|{
comment|/**    * Create an unmanaged {@link HConnection} based on the environment in which we are running the    * coprocessor. The {@link HConnection} must be externally cleaned up (we bypass the usual HTable    * cleanup mechanisms since we own everything).    * @param env environment hosting the {@link HConnection}    * @return an unmanaged {@link HConnection}.    * @throws IOException if we cannot create the basic connection    */
specifier|public
specifier|static
name|HConnection
name|getConnectionForEnvironment
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|createConnection
argument_list|(
name|env
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
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
name|connection
argument_list|,
operator|(
name|HRegionServer
operator|)
name|services
argument_list|)
return|;
block|}
block|}
return|return
name|connection
return|;
block|}
specifier|private
name|HConnection
name|delegate
decl_stmt|;
specifier|private
name|ServerName
name|serverName
decl_stmt|;
specifier|private
name|HRegionServer
name|server
decl_stmt|;
specifier|public
name|CoprocessorHConnection
parameter_list|(
name|HConnection
name|delegate
parameter_list|,
name|HRegionServer
name|server
parameter_list|)
block|{
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
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
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
name|delegate
operator|.
name|getClient
argument_list|(
name|serverName
argument_list|)
return|;
block|}
comment|// the client is attempting to write to the same regionserver, we can short-circuit to our
comment|// local regionserver
specifier|final
name|BlockingService
name|blocking
init|=
name|ClientService
operator|.
name|newReflectiveBlockingService
argument_list|(
name|this
operator|.
name|server
argument_list|)
decl_stmt|;
specifier|final
name|RpcServerInterface
name|rpc
init|=
name|this
operator|.
name|server
operator|.
name|getRpcServer
argument_list|()
decl_stmt|;
specifier|final
name|MonitoredRPCHandler
name|status
init|=
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createRPCStatus
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|status
operator|.
name|pause
argument_list|(
literal|"Setting up server-local call"
argument_list|)
expr_stmt|;
specifier|final
name|long
name|timestamp
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|BlockingRpcChannel
name|channel
init|=
operator|new
name|BlockingRpcChannel
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Message
name|callBlockingMethod
parameter_list|(
name|MethodDescriptor
name|method
parameter_list|,
name|RpcController
name|controller
parameter_list|,
name|Message
name|request
parameter_list|,
name|Message
name|responsePrototype
parameter_list|)
throws|throws
name|ServiceException
block|{
try|try
block|{
comment|// we never need a cell-scanner - everything is already fully formed
return|return
name|rpc
operator|.
name|call
argument_list|(
name|blocking
argument_list|,
name|method
argument_list|,
name|request
argument_list|,
literal|null
argument_list|,
name|timestamp
argument_list|,
name|status
argument_list|)
operator|.
name|getFirst
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
return|return
name|ClientService
operator|.
name|newBlockingStub
argument_list|(
name|channel
argument_list|)
return|;
block|}
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|delegate
operator|.
name|abort
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|isAborted
argument_list|()
return|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
specifier|public
name|HTableInterface
name|getTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
block|{
return|return
name|delegate
operator|.
name|isMasterRunning
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isTableDisabled
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isTableDisabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
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
block|{
return|return
name|delegate
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|)
return|;
block|}
specifier|public
name|HTableDescriptor
index|[]
name|listTables
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|listTables
argument_list|()
return|;
block|}
specifier|public
name|String
index|[]
name|getTableNames
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getTableNames
argument_list|()
return|;
block|}
specifier|public
name|TableName
index|[]
name|listTableNames
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|listTableNames
argument_list|()
return|;
block|}
specifier|public
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getHTableDescriptor
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getHTableDescriptor
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
specifier|public
name|void
name|clearRegionCache
parameter_list|()
block|{
name|delegate
operator|.
name|clearRegionCache
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|clearRegionCache
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|delegate
operator|.
name|clearRegionCache
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clearRegionCache
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
block|{
name|delegate
operator|.
name|clearRegionCache
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HRegionLocation
name|relocateRegion
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
specifier|public
name|HRegionLocation
name|relocateRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
specifier|public
name|void
name|updateCachedLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|,
name|Object
name|exception
parameter_list|,
name|HRegionLocation
name|source
parameter_list|)
block|{
name|delegate
operator|.
name|updateCachedLocations
argument_list|(
name|tableName
argument_list|,
name|rowkey
argument_list|,
name|exception
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateCachedLocations
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|,
name|Object
name|exception
parameter_list|,
name|HRegionLocation
name|source
parameter_list|)
block|{
name|delegate
operator|.
name|updateCachedLocations
argument_list|(
name|tableName
argument_list|,
name|rowkey
argument_list|,
name|exception
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HRegionLocation
name|locateRegion
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegion
argument_list|(
name|regionName
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegions
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegions
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|boolean
name|useCache
parameter_list|,
name|boolean
name|offlined
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegions
argument_list|(
name|tableName
argument_list|,
name|useCache
argument_list|,
name|offlined
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locateRegions
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|boolean
name|useCache
parameter_list|,
name|boolean
name|offlined
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|locateRegions
argument_list|(
name|tableName
argument_list|,
name|useCache
argument_list|,
name|offlined
argument_list|)
return|;
block|}
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
name|MasterProtos
operator|.
name|MasterService
operator|.
name|BlockingInterface
name|getMaster
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getMaster
argument_list|()
return|;
block|}
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
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|getAdmin
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getAdmin
argument_list|(
name|serverName
argument_list|)
return|;
block|}
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
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|getAdmin
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|boolean
name|getMaster
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getAdmin
argument_list|(
name|serverName
argument_list|,
name|getMaster
argument_list|)
return|;
block|}
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|reload
argument_list|)
return|;
block|}
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|reload
argument_list|)
return|;
block|}
specifier|public
name|void
name|processBatch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|delegate
operator|.
name|processBatch
argument_list|(
name|actions
argument_list|,
name|tableName
argument_list|,
name|pool
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|processBatch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|delegate
operator|.
name|processBatch
argument_list|(
name|actions
argument_list|,
name|tableName
argument_list|,
name|pool
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|processBatchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|list
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|delegate
operator|.
name|processBatchCallback
argument_list|(
name|list
argument_list|,
name|tableName
argument_list|,
name|pool
argument_list|,
name|results
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|processBatchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|list
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|delegate
operator|.
name|processBatchCallback
argument_list|(
name|list
argument_list|,
name|tableName
argument_list|,
name|pool
argument_list|,
name|results
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRegionCachePrefetch
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|boolean
name|enable
parameter_list|)
block|{
name|delegate
operator|.
name|setRegionCachePrefetch
argument_list|(
name|tableName
argument_list|,
name|enable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRegionCachePrefetch
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|boolean
name|enable
parameter_list|)
block|{
name|delegate
operator|.
name|setRegionCachePrefetch
argument_list|(
name|tableName
argument_list|,
name|enable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|getRegionCachePrefetch
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|delegate
operator|.
name|getRegionCachePrefetch
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|getRegionCachePrefetch
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
block|{
return|return
name|delegate
operator|.
name|getRegionCachePrefetch
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getCurrentNrHRS
argument_list|()
return|;
block|}
specifier|public
name|HTableDescriptor
index|[]
name|getHTableDescriptorsByTableName
parameter_list|(
name|List
argument_list|<
name|TableName
argument_list|>
name|tableNames
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getHTableDescriptorsByTableName
argument_list|(
name|tableNames
argument_list|)
return|;
block|}
specifier|public
name|HTableDescriptor
index|[]
name|getHTableDescriptors
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|getHTableDescriptors
argument_list|(
name|tableNames
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|isClosed
argument_list|()
return|;
block|}
specifier|public
name|void
name|clearCaches
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
name|delegate
operator|.
name|clearCaches
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|deleteCachedRegionLocation
parameter_list|(
name|HRegionLocation
name|location
parameter_list|)
block|{
name|delegate
operator|.
name|deleteCachedRegionLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MasterKeepAliveConnection
name|getKeepAliveMasterService
parameter_list|()
throws|throws
name|MasterNotRunningException
block|{
return|return
name|delegate
operator|.
name|getKeepAliveMasterService
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isDeadServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|delegate
operator|.
name|isDeadServer
argument_list|(
name|serverName
argument_list|)
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
literal|null
return|;
comment|// don't use nonces for coprocessor connection
block|}
block|}
end_class

end_unit

