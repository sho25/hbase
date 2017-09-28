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
name|HConstants
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
name|KeyValue
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionInfoBuilder
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
name|RegionInfoBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** A non-capture group so that this can be embedded. */
specifier|public
specifier|static
specifier|final
name|String
name|ENCODED_REGION_NAME_REGEX
init|=
literal|"(?:[a-f0-9]+)"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_REPLICA_ID
init|=
literal|0xFFFF
decl_stmt|;
comment|//TODO: Move NO_HASH to HStoreFile which is really the only place it is used.
specifier|public
specifier|static
specifier|final
name|String
name|NO_HASH
init|=
literal|null
decl_stmt|;
comment|/**    * RegionInfo for first meta region    * You cannot use this builder to make an instance of the {@link #FIRST_META_REGIONINFO}.    * Just refer to this instance. Also, while the instance is actually a MutableRI, its type is    * just RI so the mutable methods are not available (unless you go casting); it appears    * as immutable (I tried adding Immutable type but it just makes a mess).    */
comment|// TODO: How come Meta regions still do not have encoded region names? Fix.
comment|// hbase:meta,,1.1588230740 should be the hbase:meta first region name.
specifier|public
specifier|static
specifier|final
name|RegionInfo
name|FIRST_META_REGIONINFO
init|=
operator|new
name|MutableRegionInfo
argument_list|(
literal|1L
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
argument_list|)
decl_stmt|;
specifier|private
name|MutableRegionInfo
name|content
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|RegionInfoBuilder
name|newBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|RegionInfoBuilder
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RegionInfoBuilder
name|newBuilder
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
operator|new
name|RegionInfoBuilder
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
specifier|private
name|RegionInfoBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|content
operator|=
operator|new
name|MutableRegionInfo
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RegionInfoBuilder
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
operator|.
name|content
operator|=
operator|new
name|MutableRegionInfo
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionInfoBuilder
name|setStartKey
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|)
block|{
name|content
operator|.
name|setStartKey
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionInfoBuilder
name|setEndKey
parameter_list|(
name|byte
index|[]
name|endKey
parameter_list|)
block|{
name|content
operator|.
name|setEndKey
argument_list|(
name|endKey
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionInfoBuilder
name|setRegionId
parameter_list|(
name|long
name|regionId
parameter_list|)
block|{
name|content
operator|.
name|setRegionId
argument_list|(
name|regionId
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionInfoBuilder
name|setReplicaId
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
name|content
operator|.
name|setReplicaId
argument_list|(
name|replicaId
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionInfoBuilder
name|setSplit
parameter_list|(
name|boolean
name|isSplit
parameter_list|)
block|{
name|content
operator|.
name|setSplit
argument_list|(
name|isSplit
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionInfoBuilder
name|setOffline
parameter_list|(
name|boolean
name|isOffline
parameter_list|)
block|{
name|content
operator|.
name|setOffline
argument_list|(
name|isOffline
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionInfo
name|build
parameter_list|()
block|{
name|RegionInfo
name|ri
init|=
operator|new
name|MutableRegionInfo
argument_list|(
name|content
argument_list|)
decl_stmt|;
comment|// Run a late check that we are not creating default meta region.
if|if
condition|(
name|ri
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|&&
name|ri
operator|.
name|getReplicaId
argument_list|()
operator|==
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot create the default meta region; "
operator|+
literal|"use static define FIRST_META_REGIONINFO"
argument_list|)
throw|;
block|}
return|return
operator|new
name|MutableRegionInfo
argument_list|(
name|content
argument_list|)
return|;
block|}
comment|/**    * An implementation of RegionInfo that adds mutable methods so can build a RegionInfo instance.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|static
class|class
name|MutableRegionInfo
implements|implements
name|RegionInfo
implements|,
name|Comparable
argument_list|<
name|RegionInfo
argument_list|>
block|{
comment|/**      * The new format for a region name contains its encodedName at the end.      * The encoded name also serves as the directory name for the region      * in the filesystem.      *      * New region name format:      *&lt;tablename>,,&lt;startkey>,&lt;regionIdTimestamp>.&lt;encodedName>.      * where,      *&lt;encodedName> is a hex version of the MD5 hash of      *&lt;tablename>,&lt;startkey>,&lt;regionIdTimestamp>      *      * The old region name format:      *&lt;tablename>,&lt;startkey>,&lt;regionIdTimestamp>      * For region names in the old format, the encoded name is a 32-bit      * JenkinsHash integer value (in its decimal notation, string form).      *<p>      * **NOTE**      *      * The first hbase:meta region, and regions created by an older      * version of HBase (0.20 or prior) will continue to use the      * old region name format.      */
comment|// This flag is in the parent of a split while the parent is still referenced
comment|// by daughter regions.  We USED to set this flag when we disabled a table
comment|// but now table state is kept up in zookeeper as of 0.90.0 HBase.
specifier|private
name|boolean
name|offLine
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|split
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|regionId
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|replicaId
init|=
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
decl_stmt|;
specifier|private
specifier|transient
name|byte
index|[]
name|regionName
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|private
name|byte
index|[]
name|startKey
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|private
name|byte
index|[]
name|endKey
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|private
name|int
name|hashCode
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|String
name|encodedName
decl_stmt|;
specifier|private
name|byte
index|[]
name|encodedNameAsBytes
decl_stmt|;
comment|// Current TableName
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|void
name|setHashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|regionName
argument_list|)
decl_stmt|;
name|result
operator|^=
name|this
operator|.
name|regionId
expr_stmt|;
name|result
operator|^=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|startKey
argument_list|)
expr_stmt|;
name|result
operator|^=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|endKey
argument_list|)
expr_stmt|;
name|result
operator|^=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|offLine
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|tableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|replicaId
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|result
expr_stmt|;
block|}
comment|/**      * Private constructor used constructing MutableRegionInfo for the      * first meta regions      */
specifier|private
name|MutableRegionInfo
parameter_list|(
name|long
name|regionId
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
comment|// This constructor is currently private for making hbase:meta region only.
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
name|regionId
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|replicaId
expr_stmt|;
comment|// Note: First Meta region replicas names are in old format so we pass false here.
name|this
operator|.
name|regionName
operator|=
name|RegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
name|regionId
argument_list|,
name|replicaId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|setHashCode
argument_list|()
expr_stmt|;
block|}
name|MutableRegionInfo
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
name|this
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Construct MutableRegionInfo with explicit parameters      *      * @param tableName the table name      * @param startKey first key in region      * @param endKey end of key range      * @throws IllegalArgumentException      */
name|MutableRegionInfo
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|startKey
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * Construct MutableRegionInfo with explicit parameters      *      * @param tableName the table descriptor      * @param startKey first key in region      * @param endKey end of key range      * @param split true if this region has split and we have daughter regions      * regions that may or may not hold references to this region.      * @throws IllegalArgumentException      */
name|MutableRegionInfo
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|startKey
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|,
specifier|final
name|boolean
name|split
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|split
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Construct MutableRegionInfo with explicit parameters      *      * @param tableName the table descriptor      * @param startKey first key in region      * @param endKey end of key range      * @param split true if this region has split and we have daughter regions      * regions that may or may not hold references to this region.      * @param regionid Region id to use.      * @throws IllegalArgumentException      */
name|MutableRegionInfo
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|startKey
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|,
specifier|final
name|boolean
name|split
parameter_list|,
specifier|final
name|long
name|regionid
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|split
argument_list|,
name|regionid
argument_list|,
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
argument_list|)
expr_stmt|;
block|}
comment|/**      * Construct MutableRegionInfo with explicit parameters      *      * @param tableName the table descriptor      * @param startKey first key in region      * @param endKey end of key range      * @param split true if this region has split and we have daughter regions      * regions that may or may not hold references to this region.      * @param regionid Region id to use.      * @param replicaId the replicaId to use      * @throws IllegalArgumentException      */
name|MutableRegionInfo
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|startKey
parameter_list|,
specifier|final
name|byte
index|[]
name|endKey
parameter_list|,
specifier|final
name|boolean
name|split
parameter_list|,
specifier|final
name|long
name|regionid
parameter_list|,
specifier|final
name|int
name|replicaId
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|super
argument_list|()
expr_stmt|;
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"TableName cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|offLine
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
name|regionid
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|replicaId
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|replicaId
operator|>
name|MAX_REPLICA_ID
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ReplicaId cannot be greater than"
operator|+
name|MAX_REPLICA_ID
argument_list|)
throw|;
block|}
name|this
operator|.
name|regionName
operator|=
name|RegionInfo
operator|.
name|createRegionName
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|startKey
argument_list|,
name|regionId
argument_list|,
name|replicaId
argument_list|,
operator|!
name|this
operator|.
name|tableName
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|endKey
operator|=
name|endKey
operator|==
literal|null
condition|?
name|HConstants
operator|.
name|EMPTY_END_ROW
else|:
name|endKey
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|startKey
operator|==
literal|null
condition|?
name|HConstants
operator|.
name|EMPTY_START_ROW
else|:
name|startKey
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|setHashCode
argument_list|()
expr_stmt|;
block|}
comment|/**      * Construct MutableRegionInfo.      * Only for RegionInfoBuilder to use.      * @param other      */
name|MutableRegionInfo
parameter_list|(
name|MutableRegionInfo
name|other
parameter_list|,
name|boolean
name|isMetaRegion
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|getTable
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"TableName cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|tableName
operator|=
name|other
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|this
operator|.
name|offLine
operator|=
name|other
operator|.
name|isOffline
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
name|other
operator|.
name|getRegionId
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|other
operator|.
name|getReplicaId
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|replicaId
operator|>
name|MAX_REPLICA_ID
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ReplicaId cannot be greater than"
operator|+
name|MAX_REPLICA_ID
argument_list|)
throw|;
block|}
if|if
condition|(
name|isMetaRegion
condition|)
block|{
comment|// Note: First Meta region replicas names are in old format
name|this
operator|.
name|regionName
operator|=
name|RegionInfo
operator|.
name|createRegionName
argument_list|(
name|other
operator|.
name|getTable
argument_list|()
argument_list|,
literal|null
argument_list|,
name|other
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|other
operator|.
name|getReplicaId
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|regionName
operator|=
name|RegionInfo
operator|.
name|createRegionName
argument_list|(
name|other
operator|.
name|getTable
argument_list|()
argument_list|,
name|other
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|other
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|other
operator|.
name|getReplicaId
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|split
operator|=
name|other
operator|.
name|isSplit
argument_list|()
expr_stmt|;
name|this
operator|.
name|endKey
operator|=
name|other
operator|.
name|getEndKey
argument_list|()
operator|==
literal|null
condition|?
name|HConstants
operator|.
name|EMPTY_END_ROW
else|:
name|other
operator|.
name|getEndKey
argument_list|()
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|other
operator|.
name|getStartKey
argument_list|()
operator|==
literal|null
condition|?
name|HConstants
operator|.
name|EMPTY_START_ROW
else|:
name|other
operator|.
name|getStartKey
argument_list|()
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|other
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|setHashCode
argument_list|()
expr_stmt|;
block|}
comment|/**      * Construct a copy of RegionInfo as MutableRegionInfo.      * Only for RegionInfoBuilder to use.      * @param regionInfo      */
name|MutableRegionInfo
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|endKey
operator|=
name|regionInfo
operator|.
name|getEndKey
argument_list|()
expr_stmt|;
name|this
operator|.
name|offLine
operator|=
name|regionInfo
operator|.
name|isOffline
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
name|regionInfo
operator|.
name|getRegionId
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|regionInfo
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|regionInfo
operator|.
name|isSplit
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
name|regionInfo
operator|.
name|getStartKey
argument_list|()
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|regionInfo
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|this
operator|.
name|encodedName
operator|=
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|regionInfo
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
expr_stmt|;
block|}
comment|/**      * @return Return a short, printable name for this region      * (usually encoded name) for us logging.      */
annotation|@
name|Override
specifier|public
name|String
name|getShortNameToLog
parameter_list|()
block|{
return|return
name|RegionInfo
operator|.
name|prettyPrint
argument_list|(
name|this
operator|.
name|getEncodedName
argument_list|()
argument_list|)
return|;
block|}
comment|/** @return the regionId */
annotation|@
name|Override
specifier|public
name|long
name|getRegionId
parameter_list|()
block|{
return|return
name|regionId
return|;
block|}
comment|/**      * set region id.      * @param regionId      * @return MutableRegionInfo      */
specifier|public
name|MutableRegionInfo
name|setRegionId
parameter_list|(
name|long
name|regionId
parameter_list|)
block|{
name|this
operator|.
name|regionId
operator|=
name|regionId
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the regionName as an array of bytes.      * @see #getRegionNameAsString()      */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
name|regionName
return|;
block|}
comment|/**      * set region name.      * @param regionName      * @return MutableRegionInfo      */
specifier|public
name|MutableRegionInfo
name|setRegionName
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return Region name as a String for use in logging, etc.      */
annotation|@
name|Override
specifier|public
name|String
name|getRegionNameAsString
parameter_list|()
block|{
if|if
condition|(
name|RegionInfo
operator|.
name|hasEncodedName
argument_list|(
name|this
operator|.
name|regionName
argument_list|)
condition|)
block|{
comment|// new format region names already have their encoded name.
return|return
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|regionName
argument_list|)
return|;
block|}
comment|// old format. regionNameStr doesn't have the region name.
comment|//
comment|//
return|return
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|regionName
argument_list|)
operator|+
literal|"."
operator|+
name|this
operator|.
name|getEncodedName
argument_list|()
return|;
block|}
comment|/** @return the encoded region name */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|getEncodedName
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|encodedName
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|encodedName
operator|=
name|RegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|this
operator|.
name|regionName
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|encodedName
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|byte
index|[]
name|getEncodedNameAsBytes
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|encodedNameAsBytes
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|encodedNameAsBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|encodedNameAsBytes
return|;
block|}
comment|/** @return the startKey */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getStartKey
parameter_list|()
block|{
return|return
name|startKey
return|;
block|}
comment|/**      * @param startKey      * @return MutableRegionInfo      */
specifier|public
name|MutableRegionInfo
name|setStartKey
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|)
block|{
name|this
operator|.
name|startKey
operator|=
name|startKey
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** @return the endKey */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getEndKey
parameter_list|()
block|{
return|return
name|endKey
return|;
block|}
comment|/**      * @param endKey      * @return MutableRegionInfo      */
specifier|public
name|MutableRegionInfo
name|setEndKey
parameter_list|(
name|byte
index|[]
name|endKey
parameter_list|)
block|{
name|this
operator|.
name|endKey
operator|=
name|endKey
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Get current table name of the region      * @return TableName      */
annotation|@
name|Override
specifier|public
name|TableName
name|getTable
parameter_list|()
block|{
comment|// This method name should be getTableName but there was already a method getTableName
comment|// that returned a byte array.  It is unfortunate given everywhere else, getTableName returns
comment|// a TableName instance.
if|if
condition|(
name|tableName
operator|==
literal|null
operator|||
name|tableName
operator|.
name|getName
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|tableName
operator|=
name|RegionInfo
operator|.
name|getTable
argument_list|(
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|tableName
return|;
block|}
comment|/**      * Returns true if the given inclusive range of rows is fully contained      * by this region. For example, if the region is foo,a,g and this is      * passed ["b","c"] or ["a","c"] it will return true, but if this is passed      * ["b","z"] it will return false.      * @throws IllegalArgumentException if the range passed is invalid (ie. end&lt; start)      */
annotation|@
name|Override
specifier|public
name|boolean
name|containsRange
parameter_list|(
name|byte
index|[]
name|rangeStartKey
parameter_list|,
name|byte
index|[]
name|rangeEndKey
parameter_list|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|rangeStartKey
argument_list|,
name|rangeEndKey
argument_list|)
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid range: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rangeStartKey
argument_list|)
operator|+
literal|"> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rangeEndKey
argument_list|)
argument_list|)
throw|;
block|}
name|boolean
name|firstKeyInRange
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|rangeStartKey
argument_list|,
name|startKey
argument_list|)
operator|>=
literal|0
decl_stmt|;
name|boolean
name|lastKeyInRange
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|rangeEndKey
argument_list|,
name|endKey
argument_list|)
operator|<
literal|0
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|endKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
decl_stmt|;
return|return
name|firstKeyInRange
operator|&&
name|lastKeyInRange
return|;
block|}
comment|/**      * Return true if the given row falls in this region.      */
annotation|@
name|Override
specifier|public
name|boolean
name|containsRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|row
argument_list|,
name|startKey
argument_list|)
operator|>=
literal|0
operator|&&
operator|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|row
argument_list|,
name|endKey
argument_list|)
operator|<
literal|0
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|endKey
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
operator|)
return|;
block|}
comment|/**      * @return true if this region is from hbase:meta      */
annotation|@
name|Override
specifier|public
name|boolean
name|isMetaTable
parameter_list|()
block|{
return|return
name|isMetaRegion
argument_list|()
return|;
block|}
comment|/** @return true if this region is a meta region */
annotation|@
name|Override
specifier|public
name|boolean
name|isMetaRegion
parameter_list|()
block|{
return|return
name|tableName
operator|.
name|equals
argument_list|(
name|FIRST_META_REGIONINFO
operator|.
name|getTable
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * @return true if this region is from a system table      */
annotation|@
name|Override
specifier|public
name|boolean
name|isSystemTable
parameter_list|()
block|{
return|return
name|tableName
operator|.
name|isSystemTable
argument_list|()
return|;
block|}
comment|/**      * @return True if has been split and has daughters.      */
annotation|@
name|Override
specifier|public
name|boolean
name|isSplit
parameter_list|()
block|{
return|return
name|this
operator|.
name|split
return|;
block|}
comment|/**      * @param split set split status      * @return MutableRegionInfo      */
specifier|public
name|MutableRegionInfo
name|setSplit
parameter_list|(
name|boolean
name|split
parameter_list|)
block|{
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return True if this region is offline.      */
annotation|@
name|Override
specifier|public
name|boolean
name|isOffline
parameter_list|()
block|{
return|return
name|this
operator|.
name|offLine
return|;
block|}
comment|/**      * The parent of a region split is offline while split daughters hold      * references to the parent. Offlined regions are closed.      * @param offLine Set online/offline status.      * @return MutableRegionInfo      */
specifier|public
name|MutableRegionInfo
name|setOffline
parameter_list|(
name|boolean
name|offLine
parameter_list|)
block|{
name|this
operator|.
name|offLine
operator|=
name|offLine
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return True if this is a split parent region.      */
annotation|@
name|Override
specifier|public
name|boolean
name|isSplitParent
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isSplit
argument_list|()
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|isOffline
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region is split but NOT offline: "
operator|+
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Returns the region replica id      * @return returns region replica id      */
annotation|@
name|Override
specifier|public
name|int
name|getReplicaId
parameter_list|()
block|{
return|return
name|replicaId
return|;
block|}
specifier|public
name|MutableRegionInfo
name|setReplicaId
parameter_list|(
name|int
name|replicaId
parameter_list|)
block|{
name|this
operator|.
name|replicaId
operator|=
name|replicaId
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @see java.lang.Object#toString()      */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{ENCODED => "
operator|+
name|getEncodedName
argument_list|()
operator|+
literal|", "
operator|+
name|HConstants
operator|.
name|NAME
operator|+
literal|" => '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|regionName
argument_list|)
operator|+
literal|"', STARTKEY => '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|startKey
argument_list|)
operator|+
literal|"', ENDKEY => '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|endKey
argument_list|)
operator|+
literal|"'"
operator|+
operator|(
name|isOffline
argument_list|()
condition|?
literal|", OFFLINE => true"
else|:
literal|""
operator|)
operator|+
operator|(
name|isSplit
argument_list|()
condition|?
literal|", SPLIT => true"
else|:
literal|""
operator|)
operator|+
operator|(
operator|(
name|replicaId
operator|>
literal|0
operator|)
condition|?
literal|", REPLICA_ID => "
operator|+
name|replicaId
else|:
literal|""
operator|)
operator|+
literal|"}"
return|;
block|}
comment|/**      * @param o      * @see java.lang.Object#equals(java.lang.Object)      */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|RegionInfo
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|this
operator|.
name|compareTo
argument_list|(
operator|(
name|RegionInfo
operator|)
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**      * @see java.lang.Object#hashCode()      */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|hashCode
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|RegionInfo
name|other
parameter_list|)
block|{
return|return
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|this
argument_list|,
name|other
argument_list|)
return|;
block|}
comment|/**      * @return Comparator to use comparing {@link KeyValue}s.      * @deprecated Use Region#getCellComparator().  deprecated for hbase 2.0, remove for hbase 3.0      */
annotation|@
name|Deprecated
specifier|public
name|KeyValue
operator|.
name|KVComparator
name|getComparator
parameter_list|()
block|{
return|return
name|isMetaRegion
argument_list|()
condition|?
name|KeyValue
operator|.
name|META_COMPARATOR
else|:
name|KeyValue
operator|.
name|COMPARATOR
return|;
block|}
block|}
block|}
end_class

end_unit
