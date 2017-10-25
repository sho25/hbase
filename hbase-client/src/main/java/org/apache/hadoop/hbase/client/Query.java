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
name|shaded
operator|.
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
name|filter
operator|.
name|Filter
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
name|io
operator|.
name|TimeRange
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
name|access
operator|.
name|AccessControlConstants
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
name|access
operator|.
name|AccessControlUtil
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
name|access
operator|.
name|Permission
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
name|visibility
operator|.
name|Authorizations
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
name|visibility
operator|.
name|VisibilityConstants
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
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
name|common
operator|.
name|collect
operator|.
name|ListMultimap
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

begin_comment
comment|/**  * Base class for HBase read operations; e.g. Scan and Get.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|Query
extends|extends
name|OperationWithAttributes
block|{
specifier|private
specifier|static
specifier|final
name|String
name|ISOLATION_LEVEL
init|=
literal|"_isolationlevel_"
decl_stmt|;
specifier|protected
name|Filter
name|filter
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|targetReplicaId
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|Consistency
name|consistency
init|=
name|Consistency
operator|.
name|STRONG
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|TimeRange
argument_list|>
name|colFamTimeRangeMap
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|protected
name|Boolean
name|loadColumnFamiliesOnDemand
init|=
literal|null
decl_stmt|;
comment|/**    * @return Filter    */
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
comment|/**    * Apply the specified server-side filter when performing the Query. Only    * {@link Filter#filterCell(org.apache.hadoop.hbase.Cell)} is called AFTER all tests for ttl,    * column match, deletes and column family's max versions have been run.    * @param filter filter to run on the server    * @return this for invocation chaining    */
specifier|public
name|Query
name|setFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Sets the authorizations to be used by this Query    * @param authorizations    */
specifier|public
name|Query
name|setAuthorizations
parameter_list|(
name|Authorizations
name|authorizations
parameter_list|)
block|{
name|this
operator|.
name|setAttribute
argument_list|(
name|VisibilityConstants
operator|.
name|VISIBILITY_LABELS_ATTR_KEY
argument_list|,
name|ProtobufUtil
operator|.
name|toAuthorizations
argument_list|(
name|authorizations
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return The authorizations this Query is associated with.    * @throws DeserializationException    */
specifier|public
name|Authorizations
name|getAuthorizations
parameter_list|()
throws|throws
name|DeserializationException
block|{
name|byte
index|[]
name|authorizationsBytes
init|=
name|this
operator|.
name|getAttribute
argument_list|(
name|VisibilityConstants
operator|.
name|VISIBILITY_LABELS_ATTR_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorizationsBytes
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|ProtobufUtil
operator|.
name|toAuthorizations
argument_list|(
name|authorizationsBytes
argument_list|)
return|;
block|}
comment|/**    * @return The serialized ACL for this operation, or null if none    */
specifier|public
name|byte
index|[]
name|getACL
parameter_list|()
block|{
return|return
name|getAttribute
argument_list|(
name|AccessControlConstants
operator|.
name|OP_ATTRIBUTE_ACL
argument_list|)
return|;
block|}
comment|/**    * @param user User short name    * @param perms Permissions for the user    */
specifier|public
name|Query
name|setACL
parameter_list|(
name|String
name|user
parameter_list|,
name|Permission
name|perms
parameter_list|)
block|{
name|setAttribute
argument_list|(
name|AccessControlConstants
operator|.
name|OP_ATTRIBUTE_ACL
argument_list|,
name|AccessControlUtil
operator|.
name|toUsersAndPermissions
argument_list|(
name|user
argument_list|,
name|perms
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @param perms A map of permissions for a user or users    */
specifier|public
name|Query
name|setACL
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|perms
parameter_list|)
block|{
name|ListMultimap
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|permMap
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Permission
argument_list|>
name|entry
range|:
name|perms
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|permMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|setAttribute
argument_list|(
name|AccessControlConstants
operator|.
name|OP_ATTRIBUTE_ACL
argument_list|,
name|AccessControlUtil
operator|.
name|toUsersAndPermissions
argument_list|(
name|permMap
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Returns the consistency level for this operation    * @return the consistency level    */
specifier|public
name|Consistency
name|getConsistency
parameter_list|()
block|{
return|return
name|consistency
return|;
block|}
comment|/**    * Sets the consistency level for this operation    * @param consistency the consistency level    */
specifier|public
name|Query
name|setConsistency
parameter_list|(
name|Consistency
name|consistency
parameter_list|)
block|{
name|this
operator|.
name|consistency
operator|=
name|consistency
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Specify region replica id where Query will fetch data from. Use this together with    * {@link #setConsistency(Consistency)} passing {@link Consistency#TIMELINE} to read data from    * a specific replicaId.    *<br><b> Expert:</b>This is an advanced API exposed. Only use it if you know what you are doing    * @param Id    */
specifier|public
name|Query
name|setReplicaId
parameter_list|(
name|int
name|Id
parameter_list|)
block|{
name|this
operator|.
name|targetReplicaId
operator|=
name|Id
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Returns region replica id where Query will fetch data from.    * @return region replica id or -1 if not set.    */
specifier|public
name|int
name|getReplicaId
parameter_list|()
block|{
return|return
name|this
operator|.
name|targetReplicaId
return|;
block|}
comment|/**    * Set the isolation level for this query. If the    * isolation level is set to READ_UNCOMMITTED, then    * this query will return data from committed and    * uncommitted transactions. If the isolation level    * is set to READ_COMMITTED, then this query will return    * data from committed transactions only. If a isolation    * level is not explicitly set on a Query, then it    * is assumed to be READ_COMMITTED.    * @param level IsolationLevel for this query    */
specifier|public
name|Query
name|setIsolationLevel
parameter_list|(
name|IsolationLevel
name|level
parameter_list|)
block|{
name|setAttribute
argument_list|(
name|ISOLATION_LEVEL
argument_list|,
name|level
operator|.
name|toBytes
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return The isolation level of this query.    * If no isolation level was set for this query object,    * then it returns READ_COMMITTED.    * @return The IsolationLevel for this query    */
specifier|public
name|IsolationLevel
name|getIsolationLevel
parameter_list|()
block|{
name|byte
index|[]
name|attr
init|=
name|getAttribute
argument_list|(
name|ISOLATION_LEVEL
argument_list|)
decl_stmt|;
return|return
name|attr
operator|==
literal|null
condition|?
name|IsolationLevel
operator|.
name|READ_COMMITTED
else|:
name|IsolationLevel
operator|.
name|fromBytes
argument_list|(
name|attr
argument_list|)
return|;
block|}
comment|/**    * Set the value indicating whether loading CFs on demand should be allowed (cluster    * default is false). On-demand CF loading doesn't load column families until necessary, e.g.    * if you filter on one column, the other column family data will be loaded only for the rows    * that are included in result, not all rows like in normal case.    * With column-specific filters, like SingleColumnValueFilter w/filterIfMissing == true,    * this can deliver huge perf gains when there's a cf with lots of data; however, it can    * also lead to some inconsistent results, as follows:    * - if someone does a concurrent update to both column families in question you may get a row    *   that never existed, e.g. for { rowKey = 5, { cat_videos =&gt; 1 }, { video =&gt; "my cat" } }    *   someone puts rowKey 5 with { cat_videos =&gt; 0 }, { video =&gt; "my dog" }, concurrent scan    *   filtering on "cat_videos == 1" can get { rowKey = 5, { cat_videos =&gt; 1 },    *   { video =&gt; "my dog" } }.    * - if there's a concurrent split and you have more than 2 column families, some rows may be    *   missing some column families.    */
specifier|public
name|Query
name|setLoadColumnFamiliesOnDemand
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|loadColumnFamiliesOnDemand
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get the raw loadColumnFamiliesOnDemand setting; if it's not set, can be null.    */
specifier|public
name|Boolean
name|getLoadColumnFamiliesOnDemandValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|loadColumnFamiliesOnDemand
return|;
block|}
comment|/**    * Get the logical value indicating whether on-demand CF loading should be allowed.    */
specifier|public
name|boolean
name|doLoadColumnFamiliesOnDemand
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|loadColumnFamiliesOnDemand
operator|!=
literal|null
operator|)
operator|&&
name|this
operator|.
name|loadColumnFamiliesOnDemand
return|;
block|}
comment|/**    * Get versions of columns only within the specified timestamp range,    * [minStamp, maxStamp) on a per CF bases.  Note, default maximum versions to return is 1.  If    * your time range spans more than one version and you want all versions    * returned, up the number of versions beyond the default.    * Column Family time ranges take precedence over the global time range.    *    * @param cf       the column family for which you want to restrict    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @return this    */
specifier|public
name|Query
name|setColumnFamilyTimeRange
parameter_list|(
name|byte
index|[]
name|cf
parameter_list|,
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
block|{
name|colFamTimeRangeMap
operator|.
name|put
argument_list|(
name|cf
argument_list|,
operator|new
name|TimeRange
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * @return A map of column families to time ranges    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|TimeRange
argument_list|>
name|getColumnFamilyTimeRange
parameter_list|()
block|{
return|return
name|this
operator|.
name|colFamTimeRangeMap
return|;
block|}
block|}
end_class

end_unit

