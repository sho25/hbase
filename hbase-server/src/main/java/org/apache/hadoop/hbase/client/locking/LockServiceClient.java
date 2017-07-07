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
operator|.
name|client
operator|.
name|locking
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
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|Abortable
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
name|HBaseInterfaceAudience
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
name|hbase
operator|.
name|client
operator|.
name|NonceGenerator
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
name|ProtobufUtil
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
name|LockServiceProtos
operator|.
name|LockRequest
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
name|LockServiceProtos
operator|.
name|LockType
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
name|LockServiceProtos
operator|.
name|LockService
import|;
end_import

begin_comment
comment|/**  * Helper class to create "master locks" for namespaces, tables and regions.  * DEV-NOTE: At the moment this class is used only by the RS for MOB,  *           to prevent other MOB compaction to conflict.  *           The RS has already the stub of the LockService, so we have only one constructor that  *           takes the LockService stub. If in the future we are going to use this in other places  *           we should add a constructor that from conf or connection, creates the stub.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|LockServiceClient
block|{
specifier|private
specifier|final
name|LockService
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|NonceGenerator
name|ng
decl_stmt|;
specifier|public
name|LockServiceClient
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|LockService
operator|.
name|BlockingInterface
name|stub
parameter_list|,
specifier|final
name|NonceGenerator
name|ng
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
name|this
operator|.
name|ng
operator|=
name|ng
expr_stmt|;
block|}
comment|/**    * Create a new EntityLock object to acquire an exclusive or shared lock on a table.    * Internally, the table namespace will also be locked in shared mode.    */
specifier|public
name|EntityLock
name|tableLock
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|boolean
name|exclusive
parameter_list|,
specifier|final
name|String
name|description
parameter_list|,
specifier|final
name|Abortable
name|abort
parameter_list|)
block|{
name|LockRequest
name|lockRequest
init|=
name|buildLockRequest
argument_list|(
name|exclusive
condition|?
name|LockType
operator|.
name|EXCLUSIVE
else|:
name|LockType
operator|.
name|SHARED
argument_list|,
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|description
argument_list|,
name|ng
operator|.
name|getNonceGroup
argument_list|()
argument_list|,
name|ng
operator|.
name|newNonce
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|EntityLock
argument_list|(
name|conf
argument_list|,
name|stub
argument_list|,
name|lockRequest
argument_list|,
name|abort
argument_list|)
return|;
block|}
comment|/**    * LocCreate a new EntityLock object to acquire exclusive lock on a namespace.    * Clients can not acquire shared locks on namespace.    */
specifier|public
name|EntityLock
name|namespaceLock
parameter_list|(
name|String
name|namespace
parameter_list|,
name|String
name|description
parameter_list|,
name|Abortable
name|abort
parameter_list|)
block|{
name|LockRequest
name|lockRequest
init|=
name|buildLockRequest
argument_list|(
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
name|namespace
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|description
argument_list|,
name|ng
operator|.
name|getNonceGroup
argument_list|()
argument_list|,
name|ng
operator|.
name|newNonce
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|EntityLock
argument_list|(
name|conf
argument_list|,
name|stub
argument_list|,
name|lockRequest
argument_list|,
name|abort
argument_list|)
return|;
block|}
comment|/**    * Create a new EntityLock object to acquire exclusive lock on multiple regions of same tables.    * Internally, the table and its namespace will also be locked in shared mode.    */
specifier|public
name|EntityLock
name|regionLock
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
parameter_list|,
name|String
name|description
parameter_list|,
name|Abortable
name|abort
parameter_list|)
block|{
name|LockRequest
name|lockRequest
init|=
name|buildLockRequest
argument_list|(
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|regionInfos
argument_list|,
name|description
argument_list|,
name|ng
operator|.
name|getNonceGroup
argument_list|()
argument_list|,
name|ng
operator|.
name|newNonce
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|EntityLock
argument_list|(
name|conf
argument_list|,
name|stub
argument_list|,
name|lockRequest
argument_list|,
name|abort
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|LockRequest
name|buildLockRequest
parameter_list|(
specifier|final
name|LockType
name|type
parameter_list|,
specifier|final
name|String
name|namespace
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
parameter_list|,
specifier|final
name|String
name|description
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
block|{
specifier|final
name|LockRequest
operator|.
name|Builder
name|builder
init|=
name|LockRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setLockType
argument_list|(
name|type
argument_list|)
operator|.
name|setNonceGroup
argument_list|(
name|nonceGroup
argument_list|)
operator|.
name|setNonce
argument_list|(
name|nonce
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionInfos
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionInfos
control|)
block|{
name|builder
operator|.
name|addRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|namespace
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setNamespace
argument_list|(
name|namespace
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|setDescription
argument_list|(
name|description
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

