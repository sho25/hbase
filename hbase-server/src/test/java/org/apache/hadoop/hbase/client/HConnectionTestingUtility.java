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
name|RegionLocations
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
name|HRegionInfo
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
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
name|ipc
operator|.
name|RpcControllerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * {@link HConnection} testing utility.  */
end_comment

begin_class
specifier|public
class|class
name|HConnectionTestingUtility
block|{
comment|/*    * Not part of {@link HBaseTestingUtility} because this class is not    * in same package as {@link HConnection}.  Would have to reveal ugly    * {@link HConnectionManager} innards to HBaseTestingUtility to give it access.    */
comment|/**    * Get a Mocked {@link HConnection} that goes with the passed<code>conf</code>    * configuration instance.  Minimally the mock will return    *<code>conf</conf> when {@link HConnection#getConfiguration()} is invoked.    * Be sure to shutdown the connection when done by calling    * {@link HConnectionManager#deleteConnection(Configuration)} else it    * will stick around; this is probably not what you want.    * @param conf configuration    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|ClusterConnection
name|getMockedConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
block|{
name|HConnectionKey
name|connectionKey
init|=
operator|new
name|HConnectionKey
argument_list|(
name|conf
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
init|)
block|{
name|HConnectionImplementation
name|connection
init|=
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
operator|.
name|get
argument_list|(
name|connectionKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|connection
operator|==
literal|null
condition|)
block|{
name|connection
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HConnectionImplementation
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
operator|.
name|put
argument_list|(
name|connectionKey
argument_list|,
name|connection
argument_list|)
expr_stmt|;
block|}
return|return
name|connection
return|;
block|}
block|}
comment|/**    * Calls {@link #getMockedConnection(Configuration)} and then mocks a few    * more of the popular {@link HConnection} methods so they do 'normal'    * operation (see return doc below for list). Be sure to shutdown the    * connection when done by calling    * {@link HConnectionManager#deleteConnection(Configuration)} else it    * will stick around; this is probably not what you want.    *    * @param conf Configuration to use    * @param admin An AdminProtocol; can be null but is usually    * itself a mock.    * @param client A ClientProtocol; can be null but is usually    * itself a mock.    * @param sn ServerName to include in the region location returned by this    *<code>connection</code>    * @param hri HRegionInfo to include in the location returned when    * getRegionLocation is called on the mocked connection    * @return Mock up a connection that returns a {@link Configuration} when    * {@link HConnection#getConfiguration()} is called, a 'location' when    * {@link HConnection#getRegionLocation(org.apache.hadoop.hbase.TableName, byte[], boolean)} is called,    * and that returns the passed {@link AdminProtos.AdminService.BlockingInterface} instance when    * {@link HConnection#getAdmin(ServerName)} is called, returns the passed    * {@link ClientProtos.ClientService.BlockingInterface} instance when    * {@link HConnection#getClient(ServerName)} is called (Be sure to call    * {@link HConnectionManager#deleteConnection(Configuration)}    * when done with this mocked Connection.    * @throws IOException    */
specifier|public
specifier|static
name|ClusterConnection
name|getMockedConnectionAndDecorate
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|admin
parameter_list|,
specifier|final
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|client
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|HConnectionImplementation
name|c
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HConnectionImplementation
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
operator|.
name|put
argument_list|(
operator|new
name|HConnectionKey
argument_list|(
name|conf
argument_list|)
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|c
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Make it so we return a particular location when asked.
specifier|final
name|HRegionLocation
name|loc
init|=
operator|new
name|HRegionLocation
argument_list|(
name|hri
argument_list|,
name|sn
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getRegionLocation
argument_list|(
operator|(
name|TableName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|loc
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|locateRegion
argument_list|(
operator|(
name|TableName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|loc
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|locateRegion
argument_list|(
operator|(
name|TableName
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|RegionLocations
argument_list|(
name|loc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
comment|// If a call to getAdmin, return this implementation.
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getAdmin
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|admin
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
comment|// If a call to getClient, return this client.
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getClient
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
name|NonceGenerator
name|ng
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|NonceGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getNonceGenerator
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ng
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getAsyncProcess
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|AsyncProcess
argument_list|(
name|c
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|false
argument_list|,
name|RpcControllerFactory
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|c
argument_list|)
operator|.
name|incCount
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|c
argument_list|)
operator|.
name|decCount
argument_list|()
expr_stmt|;
return|return
name|c
return|;
block|}
comment|/**    * Get a Mockito spied-upon {@link HConnection} that goes with the passed    *<code>conf</code> configuration instance.    * Be sure to shutdown the connection when done by calling    * {@link HConnectionManager#deleteConnection(Configuration)} else it    * will stick around; this is probably not what you want.    * @param conf configuration    * @return HConnection object for<code>conf</code>    * @throws ZooKeeperConnectionException    * @see @link    * {http://mockito.googlecode.com/svn/branches/1.6/javadoc/org/mockito/Mockito.html#spy(T)}    */
specifier|public
specifier|static
name|HConnection
name|getSpiedConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|HConnectionKey
name|connectionKey
init|=
operator|new
name|HConnectionKey
argument_list|(
name|conf
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
init|)
block|{
name|HConnectionImplementation
name|connection
init|=
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
operator|.
name|get
argument_list|(
name|connectionKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|connection
operator|==
literal|null
condition|)
block|{
name|connection
operator|=
name|Mockito
operator|.
name|spy
argument_list|(
operator|new
name|HConnectionImplementation
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
operator|.
name|put
argument_list|(
name|connectionKey
argument_list|,
name|connection
argument_list|)
expr_stmt|;
block|}
return|return
name|connection
return|;
block|}
block|}
comment|/**    * @return Count of extant connection instances    */
specifier|public
specifier|static
name|int
name|getConnectionCount
parameter_list|()
block|{
synchronized|synchronized
init|(
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
init|)
block|{
return|return
name|ConnectionManager
operator|.
name|CONNECTION_INSTANCES
operator|.
name|size
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

