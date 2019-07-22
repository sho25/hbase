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
name|util
operator|.
name|Map
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
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|collections4
operator|.
name|MapUtils
import|;
end_import

begin_comment
comment|/**  * The POJO equivalent of HBaseProtos.SnapshotDescription  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SnapshotDescription
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|table
decl_stmt|;
specifier|private
specifier|final
name|SnapshotType
name|snapShotType
decl_stmt|;
specifier|private
specifier|final
name|String
name|owner
decl_stmt|;
specifier|private
specifier|final
name|long
name|creationTime
decl_stmt|;
specifier|private
specifier|final
name|long
name|ttl
decl_stmt|;
specifier|private
specifier|final
name|int
name|version
decl_stmt|;
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
operator|(
name|TableName
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated since 2.0.0 and will be removed in 3.0.0. Use the version with the TableName    *   instance instead.    * @see #SnapshotDescription(String, TableName)    * @see<a href="https://issues.apache.org/jira/browse/HBASE-16892">HBASE-16892</a>    */
annotation|@
name|Deprecated
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|TableName
name|table
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|table
argument_list|,
name|SnapshotType
operator|.
name|DISABLED
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated since 2.0.0 and will be removed in 3.0.0. Use the version with the TableName    *   instance instead.    * @see #SnapshotDescription(String, TableName, SnapshotType)    * @see<a href="https://issues.apache.org/jira/browse/HBASE-16892">HBASE-16892</a>    */
annotation|@
name|Deprecated
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|TableName
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|table
argument_list|,
name|type
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see #SnapshotDescription(String, TableName, SnapshotType, String)    * @see<a href="https://issues.apache.org/jira/browse/HBASE-16892">HBASE-16892</a>    * @deprecated since 2.0.0 and will be removed in 3.0.0. Use the version with the TableName    *   instance instead.    */
annotation|@
name|Deprecated
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|String
name|owner
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|,
name|type
argument_list|,
name|owner
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|TableName
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|String
name|owner
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|table
argument_list|,
name|type
argument_list|,
name|owner
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see #SnapshotDescription(String, TableName, SnapshotType, String, long, int, Map)    * @see<a href="https://issues.apache.org/jira/browse/HBASE-16892">HBASE-16892</a>    * @deprecated since 2.0.0 and will be removed in 3.0.0. Use the version with the TableName    *   instance instead.    */
annotation|@
name|Deprecated
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|String
name|owner
parameter_list|,
name|long
name|creationTime
parameter_list|,
name|int
name|version
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|,
name|type
argument_list|,
name|owner
argument_list|,
name|creationTime
argument_list|,
name|version
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * SnapshotDescription Parameterized Constructor    *    * @param name          Name of the snapshot    * @param table         TableName associated with the snapshot    * @param type          Type of the snapshot - enum SnapshotType    * @param owner         Snapshot Owner    * @param creationTime  Creation time for Snapshot    * @param version       Snapshot Version    * @param snapshotProps Additional properties for snapshot e.g. TTL    */
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|TableName
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|String
name|owner
parameter_list|,
name|long
name|creationTime
parameter_list|,
name|int
name|version
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|snapshotProps
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|snapShotType
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|owner
operator|=
name|owner
expr_stmt|;
name|this
operator|.
name|creationTime
operator|=
name|creationTime
expr_stmt|;
name|this
operator|.
name|ttl
operator|=
name|getTtlFromSnapshotProps
argument_list|(
name|snapshotProps
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
specifier|private
name|long
name|getTtlFromSnapshotProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|snapshotProps
parameter_list|)
block|{
return|return
name|MapUtils
operator|.
name|getLongValue
argument_list|(
name|snapshotProps
argument_list|,
literal|"TTL"
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
comment|/**    * SnapshotDescription Parameterized Constructor    *    * @param snapshotName  Name of the snapshot    * @param tableName     TableName associated with the snapshot    * @param type          Type of the snapshot - enum SnapshotType    * @param snapshotProps Additional properties for snapshot e.g. TTL    */
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|snapshotName
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|snapshotProps
parameter_list|)
block|{
name|this
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|,
name|type
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
name|snapshotProps
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
comment|/**    * @deprecated since 2.0.0 and will be removed in 3.0.0. Use {@link #getTableName()} or    *   {@link #getTableNameAsString()} instead.    * @see #getTableName()    * @see #getTableNameAsString()    * @see<a href="https://issues.apache.org/jira/browse/HBASE-16892">HBASE-16892</a>    */
annotation|@
name|Deprecated
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|getTableNameAsString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getTableNameAsString
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
operator|.
name|getNameAsString
argument_list|()
return|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
specifier|public
name|SnapshotType
name|getType
parameter_list|()
block|{
return|return
name|this
operator|.
name|snapShotType
return|;
block|}
specifier|public
name|String
name|getOwner
parameter_list|()
block|{
return|return
name|this
operator|.
name|owner
return|;
block|}
specifier|public
name|long
name|getCreationTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|creationTime
return|;
block|}
comment|// get snapshot ttl in sec
specifier|public
name|long
name|getTtl
parameter_list|()
block|{
return|return
name|ttl
return|;
block|}
specifier|public
name|int
name|getVersion
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|new
name|StringBuilder
argument_list|(
literal|"SnapshotDescription: "
argument_list|)
operator|.
name|append
argument_list|(
literal|"name = "
argument_list|)
operator|.
name|append
argument_list|(
name|name
argument_list|)
operator|.
name|append
argument_list|(
literal|"/table = "
argument_list|)
operator|.
name|append
argument_list|(
name|table
argument_list|)
operator|.
name|append
argument_list|(
literal|" /owner = "
argument_list|)
operator|.
name|append
argument_list|(
name|owner
argument_list|)
operator|.
name|append
argument_list|(
name|creationTime
operator|!=
operator|-
literal|1
condition|?
operator|(
literal|"/creationtime = "
operator|+
name|creationTime
operator|)
else|:
literal|""
argument_list|)
operator|.
name|append
argument_list|(
name|ttl
operator|!=
operator|-
literal|1
condition|?
operator|(
literal|"/ttl = "
operator|+
name|ttl
operator|)
else|:
literal|""
argument_list|)
operator|.
name|append
argument_list|(
name|version
operator|!=
operator|-
literal|1
condition|?
operator|(
literal|"/version = "
operator|+
name|version
operator|)
else|:
literal|""
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

