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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|MasterService
import|;
end_import

begin_comment
comment|/**  * An internal class that adapts a {@link HConnection}.  * HConnection is created from HConnectionManager. The default  * implementation talks to region servers over RPC since it  * doesn't know if the connection is used by one region server  * itself. This adapter makes it possible to change some of the  * default logic. Especially, when the connection is used  * internally by some the region server.  *  * @see ConnectionUtils#createShortCircuitHConnection(HConnection, ServerName,  * AdminService.BlockingInterface, ClientService.BlockingInterface)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
comment|//NOTE: DO NOT make this class public. It was made package-private on purpose.
class|class
name|ConnectionAdapter
implements|implements
name|ClusterConnection
block|{
specifier|private
specifier|final
name|ClusterConnection
name|wrappedConnection
decl_stmt|;
specifier|public
name|ConnectionAdapter
parameter_list|(
name|HConnection
name|c
parameter_list|)
block|{
name|wrappedConnection
operator|=
operator|(
name|ClusterConnection
operator|)
name|c
expr_stmt|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|abort
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|wrappedConnection
operator|.
name|isAborted
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|wrappedConnection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|wrappedConnection
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrappedConnection
operator|.
name|getAdmin
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isMasterRunning
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|,
name|splitKeys
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HTableDescriptor
index|[]
name|listTables
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrappedConnection
operator|.
name|listTables
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getTableNames
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrappedConnection
operator|.
name|getTableNames
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
index|[]
name|listTableNames
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrappedConnection
operator|.
name|listTableNames
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getHTableDescriptor
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getHTableDescriptor
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionLocations
name|locateRegionAll
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
name|wrappedConnection
operator|.
name|locateRegionAll
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRegionCache
parameter_list|()
block|{
name|wrappedConnection
operator|.
name|clearRegionCache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRegionCache
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|wrappedConnection
operator|.
name|clearRegionCache
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRegionCache
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
block|{
name|wrappedConnection
operator|.
name|clearRegionCache
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deleteCachedRegionLocation
parameter_list|(
name|HRegionLocation
name|location
parameter_list|)
block|{
name|wrappedConnection
operator|.
name|deleteCachedRegionLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
specifier|public
name|void
name|updateCachedLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
name|byte
index|[]
name|rowkey
parameter_list|,
name|Object
name|exception
parameter_list|,
name|ServerName
name|source
parameter_list|)
block|{
name|wrappedConnection
operator|.
name|updateCachedLocations
argument_list|(
name|tableName
argument_list|,
name|regionName
argument_list|,
name|rowkey
argument_list|,
name|exception
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|locateRegion
argument_list|(
name|regionName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|locateRegions
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|locateRegions
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
specifier|public
name|MasterService
operator|.
name|BlockingInterface
name|getMaster
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrappedConnection
operator|.
name|getMaster
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
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
name|wrappedConnection
operator|.
name|getAdmin
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
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
return|return
name|wrappedConnection
operator|.
name|getClient
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
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
name|wrappedConnection
operator|.
name|getAdmin
argument_list|(
name|serverName
argument_list|,
name|getMaster
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
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
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|setRegionCachePrefetch
argument_list|(
name|tableName
argument_list|,
name|enable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|setRegionCachePrefetch
argument_list|(
name|tableName
argument_list|,
name|enable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getRegionCachePrefetch
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|wrappedConnection
operator|.
name|getRegionCachePrefetch
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getRegionCachePrefetch
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrappedConnection
operator|.
name|getCurrentNrHRS
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getHTableDescriptorsByTableName
argument_list|(
name|tableNames
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|wrappedConnection
operator|.
name|getHTableDescriptors
argument_list|(
name|tableNames
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|wrappedConnection
operator|.
name|isClosed
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearCaches
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
name|wrappedConnection
operator|.
name|clearCaches
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MasterKeepAliveConnection
name|getKeepAliveMasterService
parameter_list|()
throws|throws
name|MasterNotRunningException
block|{
return|return
name|wrappedConnection
operator|.
name|getKeepAliveMasterService
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDeadServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|wrappedConnection
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
name|wrappedConnection
operator|.
name|getNonceGenerator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncProcess
name|getAsyncProcess
parameter_list|()
block|{
return|return
name|wrappedConnection
operator|.
name|getAsyncProcess
argument_list|()
return|;
block|}
block|}
end_class

end_unit

