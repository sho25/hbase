begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rsgroup
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|net
operator|.
name|HostAndPort
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
name|client
operator|.
name|ConnectionFactory
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
name|Result
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
name|Scan
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
name|Table
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
name|exceptions
operator|.
name|DeserializationException
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
name|protobuf
operator|.
name|generated
operator|.
name|RSGroupProtos
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VerifyingRSGroupAdminClient
extends|extends
name|RSGroupAdmin
block|{
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
name|RSGroupSerDe
name|serDe
decl_stmt|;
specifier|private
name|RSGroupAdmin
name|wrapped
decl_stmt|;
specifier|public
name|VerifyingRSGroupAdminClient
parameter_list|(
name|RSGroupAdmin
name|RSGroupAdmin
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|wrapped
operator|=
name|RSGroupAdmin
expr_stmt|;
name|table
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
operator|.
name|getTable
argument_list|(
name|RSGroupInfoManager
operator|.
name|RSGROUP_TABLE_NAME
argument_list|)
expr_stmt|;
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|serDe
operator|=
operator|new
name|RSGroupSerDe
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addRSGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
name|wrapped
operator|.
name|addRSGroup
argument_list|(
name|groupName
argument_list|)
expr_stmt|;
name|verify
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupInfo
parameter_list|(
name|String
name|groupName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrapped
operator|.
name|getRSGroupInfo
argument_list|(
name|groupName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupInfoOfTable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrapped
operator|.
name|getRSGroupInfoOfTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|moveServers
parameter_list|(
name|Set
argument_list|<
name|HostAndPort
argument_list|>
name|servers
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
block|{
name|wrapped
operator|.
name|moveServers
argument_list|(
name|servers
argument_list|,
name|targetGroup
argument_list|)
expr_stmt|;
name|verify
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|moveTables
parameter_list|(
name|Set
argument_list|<
name|TableName
argument_list|>
name|tables
parameter_list|,
name|String
name|targetGroup
parameter_list|)
throws|throws
name|IOException
block|{
name|wrapped
operator|.
name|moveTables
argument_list|(
name|tables
argument_list|,
name|targetGroup
argument_list|)
expr_stmt|;
name|verify
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|wrapped
operator|.
name|removeRSGroup
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|verify
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|balanceRSGroup
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrapped
operator|.
name|balanceRSGroup
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RSGroupInfo
argument_list|>
name|listRSGroups
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrapped
operator|.
name|listRSGroups
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|RSGroupInfo
name|getRSGroupOfServer
parameter_list|(
name|HostAndPort
name|hostPort
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrapped
operator|.
name|getRSGroupOfServer
argument_list|(
name|hostPort
argument_list|)
return|;
block|}
specifier|public
name|void
name|verify
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|RSGroupInfo
argument_list|>
name|groupMap
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|RSGroupInfo
argument_list|>
name|zList
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|table
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
control|)
block|{
name|RSGroupProtos
operator|.
name|RSGroupInfo
name|proto
init|=
name|RSGroupProtos
operator|.
name|RSGroupInfo
operator|.
name|parseFrom
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|RSGroupInfoManager
operator|.
name|META_FAMILY_BYTES
argument_list|,
name|RSGroupInfoManager
operator|.
name|META_QUALIFIER_BYTES
argument_list|)
argument_list|)
decl_stmt|;
name|groupMap
operator|.
name|put
argument_list|(
name|proto
operator|.
name|getName
argument_list|()
argument_list|,
name|RSGroupSerDe
operator|.
name|toGroupInfo
argument_list|(
name|proto
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|groupMap
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|wrapped
operator|.
name|listRSGroups
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|groupBasePath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|,
literal|"rsgroup"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|znode
range|:
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkw
argument_list|,
name|groupBasePath
argument_list|)
control|)
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|groupBasePath
argument_list|,
name|znode
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|ProtobufUtil
operator|.
name|expectPBMagicPrefix
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bis
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|data
argument_list|,
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
argument_list|,
name|data
operator|.
name|length
argument_list|)
decl_stmt|;
name|zList
operator|.
name|add
argument_list|(
name|RSGroupSerDe
operator|.
name|toGroupInfo
argument_list|(
name|RSGroupProtos
operator|.
name|RSGroupInfo
operator|.
name|parseFrom
argument_list|(
name|bis
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|zList
operator|.
name|size
argument_list|()
argument_list|,
name|groupMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RSGroupInfo
name|RSGroupInfo
range|:
name|zList
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|groupMap
operator|.
name|get
argument_list|(
name|RSGroupInfo
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|RSGroupInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"ZK verification failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"ZK verification failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"ZK verification failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
block|}
end_class

end_unit

