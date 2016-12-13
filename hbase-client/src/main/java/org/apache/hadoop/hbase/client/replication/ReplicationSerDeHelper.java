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
name|replication
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HBaseProtos
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
name|ZooKeeperProtos
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|Bytes
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
name|Strings
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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

begin_comment
comment|/**  * Helper for TableCFs Operations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|final
class|class
name|ReplicationSerDeHelper
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ReplicationSerDeHelper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ReplicationSerDeHelper
parameter_list|()
block|{}
specifier|public
specifier|static
name|String
name|convertToString
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
parameter_list|)
block|{
if|if
condition|(
name|namespaces
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|namespaces
argument_list|,
literal|';'
argument_list|)
return|;
block|}
comment|/** convert map to TableCFs Object */
specifier|public
specifier|static
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|convert
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCfs
parameter_list|)
block|{
if|if
condition|(
name|tableCfs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ZooKeeperProtos
operator|.
name|TableCF
argument_list|>
name|tableCFList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|TableCF
operator|.
name|Builder
name|tableCFBuilder
init|=
name|ZooKeeperProtos
operator|.
name|TableCF
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|tableCfs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|tableCFBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tableCFBuilder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|v
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|v
operator|!=
literal|null
operator|&&
operator|!
name|v
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|value
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|tableCFBuilder
operator|.
name|addFamilies
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|tableCFList
operator|.
name|add
argument_list|(
name|tableCFBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tableCFList
operator|.
name|toArray
argument_list|(
operator|new
name|ZooKeeperProtos
operator|.
name|TableCF
index|[
name|tableCFList
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|convertToString
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCfs
parameter_list|)
block|{
if|if
condition|(
name|tableCfs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|convert
argument_list|(
name|convert
argument_list|(
name|tableCfs
argument_list|)
argument_list|)
return|;
block|}
comment|/**    *  Convert string to TableCFs Object.    *  This is only for read TableCFs information from TableCF node.    *  Input String Format: ns1.table1:cf1,cf2;ns2.table2:cfA,cfB;ns3.table3.    * */
specifier|public
specifier|static
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|convert
parameter_list|(
name|String
name|tableCFsConfig
parameter_list|)
block|{
if|if
condition|(
name|tableCFsConfig
operator|==
literal|null
operator|||
name|tableCFsConfig
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ZooKeeperProtos
operator|.
name|TableCF
argument_list|>
name|tableCFList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|TableCF
operator|.
name|Builder
name|tableCFBuilder
init|=
name|ZooKeeperProtos
operator|.
name|TableCF
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|String
index|[]
name|tables
init|=
name|tableCFsConfig
operator|.
name|split
argument_list|(
literal|";"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tab
range|:
name|tables
control|)
block|{
comment|// 1 ignore empty table config
name|tab
operator|=
name|tab
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|tab
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// 2 split to "table" and "cf1,cf2"
comment|//   for each table: "table#cf1,cf2" or "table"
name|String
index|[]
name|pair
init|=
name|tab
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|String
name|tabName
init|=
name|pair
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|pair
operator|.
name|length
operator|>
literal|2
operator|||
name|tabName
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"incorrect format:"
operator|+
name|tableCFsConfig
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|tableCFBuilder
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// split namespace from tableName
name|String
name|ns
init|=
literal|"default"
decl_stmt|;
name|String
name|tName
init|=
name|tabName
decl_stmt|;
name|String
index|[]
name|dbs
init|=
name|tabName
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|dbs
operator|!=
literal|null
operator|&&
name|dbs
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|ns
operator|=
name|dbs
index|[
literal|0
index|]
expr_stmt|;
name|tName
operator|=
name|dbs
index|[
literal|1
index|]
expr_stmt|;
block|}
name|tableCFBuilder
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|ns
argument_list|,
name|tName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// 3 parse "cf1,cf2" part to List<cf>
if|if
condition|(
name|pair
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|String
index|[]
name|cfsList
init|=
name|pair
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|cf
range|:
name|cfsList
control|)
block|{
name|String
name|cfName
init|=
name|cf
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|cfName
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tableCFBuilder
operator|.
name|addFamilies
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|cfName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|tableCFList
operator|.
name|add
argument_list|(
name|tableCFBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tableCFList
operator|.
name|toArray
argument_list|(
operator|new
name|ZooKeeperProtos
operator|.
name|TableCF
index|[
name|tableCFList
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**    *  Convert TableCFs Object to String.    *  Output String Format: ns1.table1:cf1,cf2;ns2.table2:cfA,cfB;table3    * */
specifier|public
specifier|static
name|String
name|convert
parameter_list|(
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|tableCFs
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|tableCFs
operator|.
name|length
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ZooKeeperProtos
operator|.
name|TableCF
name|tableCF
init|=
name|tableCFs
index|[
name|i
index|]
decl_stmt|;
name|String
name|namespace
init|=
name|tableCF
operator|.
name|getTableName
argument_list|()
operator|.
name|getNamespace
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Strings
operator|.
name|isEmpty
argument_list|(
name|namespace
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|namespace
argument_list|)
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
operator|.
name|append
argument_list|(
name|tableCF
operator|.
name|getTableName
argument_list|()
operator|.
name|getQualifier
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|tableCF
operator|.
name|getTableName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|tableCF
operator|.
name|getFamiliesCount
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|tableCF
operator|.
name|getFamilies
argument_list|(
name|j
argument_list|)
operator|.
name|toStringUtf8
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|deleteCharAt
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|append
argument_list|(
literal|";"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|deleteCharAt
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    *  Get TableCF in TableCFs, if not exist, return null.    * */
specifier|public
specifier|static
name|ZooKeeperProtos
operator|.
name|TableCF
name|getTableCF
parameter_list|(
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|tableCFs
parameter_list|,
name|String
name|table
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|tableCFs
operator|.
name|length
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ZooKeeperProtos
operator|.
name|TableCF
name|tableCF
init|=
name|tableCFs
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|tableCF
operator|.
name|getTableName
argument_list|()
operator|.
name|getQualifier
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
operator|.
name|equals
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
name|tableCF
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    *  Parse bytes into TableCFs.    *  It is used for backward compatibility.    *  Old format bytes have no PB_MAGIC Header    * */
specifier|public
specifier|static
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|parseTableCFs
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ReplicationSerDeHelper
operator|.
name|convert
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|bytes
argument_list|)
argument_list|)
return|;
block|}
comment|/**    *  Convert tableCFs string into Map.    * */
specifier|public
specifier|static
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parseTableCFsFromConfig
parameter_list|(
name|String
name|tableCFsConfig
parameter_list|)
block|{
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|tableCFs
init|=
name|convert
argument_list|(
name|tableCFsConfig
argument_list|)
decl_stmt|;
return|return
name|convert2Map
argument_list|(
name|tableCFs
argument_list|)
return|;
block|}
comment|/**    *  Convert tableCFs Object to Map.    * */
specifier|public
specifier|static
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|convert2Map
parameter_list|(
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|tableCFs
parameter_list|)
block|{
if|if
condition|(
name|tableCFs
operator|==
literal|null
operator|||
name|tableCFs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Map
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFsMap
init|=
operator|new
name|HashMap
argument_list|<
name|TableName
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|tableCFs
operator|.
name|length
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ZooKeeperProtos
operator|.
name|TableCF
name|tableCF
init|=
name|tableCFs
index|[
name|i
index|]
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|families
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|,
name|m
init|=
name|tableCF
operator|.
name|getFamiliesCount
argument_list|()
init|;
name|j
operator|<
name|m
condition|;
name|j
operator|++
control|)
block|{
name|families
operator|.
name|add
argument_list|(
name|tableCF
operator|.
name|getFamilies
argument_list|(
name|j
argument_list|)
operator|.
name|toStringUtf8
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|families
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tableCFsMap
operator|.
name|put
argument_list|(
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|tableCF
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|families
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tableCFsMap
operator|.
name|put
argument_list|(
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|tableCF
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tableCFsMap
return|;
block|}
comment|/**    * @param bytes Content of a peer znode.    * @return ClusterKey parsed from the passed bytes.    * @throws DeserializationException    */
specifier|public
specifier|static
name|ReplicationPeerConfig
name|parsePeerFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|bytes
argument_list|)
condition|)
block|{
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
name|peer
decl_stmt|;
try|try
block|{
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|bytes
argument_list|,
name|pblen
argument_list|,
name|bytes
operator|.
name|length
operator|-
name|pblen
argument_list|)
expr_stmt|;
name|peer
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|convert
argument_list|(
name|peer
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
name|bytes
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|bytes
argument_list|)
argument_list|)
return|;
block|}
return|return
operator|new
name|ReplicationPeerConfig
argument_list|()
operator|.
name|setClusterKey
argument_list|(
literal|""
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|ReplicationPeerConfig
name|convert
parameter_list|(
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
name|peer
parameter_list|)
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
operator|new
name|ReplicationPeerConfig
argument_list|()
decl_stmt|;
if|if
condition|(
name|peer
operator|.
name|hasClusterkey
argument_list|()
condition|)
block|{
name|peerConfig
operator|.
name|setClusterKey
argument_list|(
name|peer
operator|.
name|getClusterkey
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|peer
operator|.
name|hasReplicationEndpointImpl
argument_list|()
condition|)
block|{
name|peerConfig
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|peer
operator|.
name|getReplicationEndpointImpl
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HBaseProtos
operator|.
name|BytesBytesPair
name|pair
range|:
name|peer
operator|.
name|getDataList
argument_list|()
control|)
block|{
name|peerConfig
operator|.
name|getPeerData
argument_list|()
operator|.
name|put
argument_list|(
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|pair
operator|.
name|getSecond
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HBaseProtos
operator|.
name|NameStringPair
name|pair
range|:
name|peer
operator|.
name|getConfigurationList
argument_list|()
control|)
block|{
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
operator|.
name|put
argument_list|(
name|pair
operator|.
name|getName
argument_list|()
argument_list|,
name|pair
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|TableName
argument_list|,
name|?
extends|extends
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableCFsMap
init|=
name|convert2Map
argument_list|(
name|peer
operator|.
name|getTableCfsList
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|ZooKeeperProtos
operator|.
name|TableCF
index|[
name|peer
operator|.
name|getTableCfsCount
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableCFsMap
operator|!=
literal|null
condition|)
block|{
name|peerConfig
operator|.
name|setTableCFsMap
argument_list|(
name|tableCFsMap
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ByteString
argument_list|>
name|namespacesList
init|=
name|peer
operator|.
name|getNamespacesList
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespacesList
operator|!=
literal|null
operator|&&
name|namespacesList
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ByteString
name|namespace
range|:
name|namespacesList
control|)
block|{
name|namespaces
operator|.
name|add
argument_list|(
name|namespace
operator|.
name|toStringUtf8
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|peerConfig
operator|.
name|setNamespaces
argument_list|(
name|namespaces
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|peer
operator|.
name|hasBandwidth
argument_list|()
condition|)
block|{
name|peerConfig
operator|.
name|setBandwidth
argument_list|(
name|peer
operator|.
name|getBandwidth
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|peerConfig
return|;
block|}
specifier|public
specifier|static
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
name|convert
parameter_list|(
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
block|{
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|ReplicationPeer
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setClusterkey
argument_list|(
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|peerConfig
operator|.
name|getReplicationEndpointImpl
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setReplicationEndpointImpl
argument_list|(
name|peerConfig
operator|.
name|getReplicationEndpointImpl
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
range|:
name|peerConfig
operator|.
name|getPeerData
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|addData
argument_list|(
name|HBaseProtos
operator|.
name|BytesBytesPair
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFirst
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setSecond
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|addConfiguration
argument_list|(
name|HBaseProtos
operator|.
name|NameStringPair
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|setValue
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ZooKeeperProtos
operator|.
name|TableCF
index|[]
name|tableCFs
init|=
name|convert
argument_list|(
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableCFs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tableCFs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|addTableCfs
argument_list|(
name|tableCFs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
decl_stmt|;
if|if
condition|(
name|namespaces
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|namespace
range|:
name|namespaces
control|)
block|{
name|builder
operator|.
name|addNamespaces
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|namespace
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|setBandwidth
argument_list|(
name|peerConfig
operator|.
name|getBandwidth
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * @param peerConfig    * @return Serialized protobuf of<code>peerConfig</code> with pb magic prefix prepended suitable    *         for use as content of a this.peersZNode; i.e. the content of PEER_ID znode under    *         /hbase/replication/peers/PEER_ID    */
specifier|public
specifier|static
name|byte
index|[]
name|toByteArray
parameter_list|(
specifier|final
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|convert
argument_list|(
name|peerConfig
argument_list|)
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|bytes
argument_list|)
return|;
block|}
block|}
end_class

end_unit

