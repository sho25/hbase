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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_comment
comment|/**  * TableDescriptor contains the details about an HBase table such as the descriptors of  * all the column families, is the table a catalog table,<code> hbase:meta</code>,  * if the table is read only, the maximum size of the memstore,  * when the region split should occur, coprocessors associated with it etc...  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|TableDescriptor
block|{
annotation|@
name|InterfaceAudience
operator|.
name|Private
name|Comparator
argument_list|<
name|TableDescriptor
argument_list|>
name|COMPARATOR
init|=
name|getComparator
argument_list|(
name|ColumnFamilyDescriptor
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
name|Comparator
argument_list|<
name|TableDescriptor
argument_list|>
name|COMPARATOR_IGNORE_REPLICATION
init|=
name|getComparator
argument_list|(
name|ColumnFamilyDescriptor
operator|.
name|COMPARATOR_IGNORE_REPLICATION
argument_list|)
decl_stmt|;
specifier|static
name|Comparator
argument_list|<
name|TableDescriptor
argument_list|>
name|getComparator
parameter_list|(
name|Comparator
argument_list|<
name|ColumnFamilyDescriptor
argument_list|>
name|cfComparator
parameter_list|)
block|{
return|return
parameter_list|(
name|TableDescriptor
name|lhs
parameter_list|,
name|TableDescriptor
name|rhs
parameter_list|)
lambda|->
block|{
name|int
name|result
init|=
name|lhs
operator|.
name|getTableName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|rhs
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
name|Collection
argument_list|<
name|ColumnFamilyDescriptor
argument_list|>
name|lhsFamilies
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|lhs
operator|.
name|getColumnFamilies
argument_list|()
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|ColumnFamilyDescriptor
argument_list|>
name|rhsFamilies
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|rhs
operator|.
name|getColumnFamilies
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|=
name|Integer
operator|.
name|compare
argument_list|(
name|lhsFamilies
operator|.
name|size
argument_list|()
argument_list|,
name|rhsFamilies
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
for|for
control|(
name|Iterator
argument_list|<
name|ColumnFamilyDescriptor
argument_list|>
name|it
init|=
name|lhsFamilies
operator|.
name|iterator
argument_list|()
init|,
name|it2
init|=
name|rhsFamilies
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|result
operator|=
name|cfComparator
operator|.
name|compare
argument_list|(
name|it
operator|.
name|next
argument_list|()
argument_list|,
name|it2
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
comment|// punt on comparison for ordering, just calculate difference
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|lhs
operator|.
name|getValues
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|,
name|rhs
operator|.
name|getValues
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|)
return|;
block|}
return|;
block|}
comment|/**    * Returns the count of the column families of the table.    *    * @return Count of column families of the table    */
name|int
name|getColumnFamilyCount
parameter_list|()
function_decl|;
comment|/**    * Return the list of attached co-processor represented by their name    * className    *    * @return The list of co-processors classNames    */
name|Collection
argument_list|<
name|String
argument_list|>
name|getCoprocessors
parameter_list|()
function_decl|;
comment|/**    * Returns the durability setting for the table.    *    * @return durability setting for the table.    */
name|Durability
name|getDurability
parameter_list|()
function_decl|;
comment|/**    * Returns an unmodifiable collection of all the {@link ColumnFamilyDescriptor} of    * all the column families of the table.    *    * @return An array of {@link ColumnFamilyDescriptor} of all the column    * families.    */
name|ColumnFamilyDescriptor
index|[]
name|getColumnFamilies
parameter_list|()
function_decl|;
comment|/**    * Returns all the column family names of the current table. The map of    * TableDescriptor contains mapping of family name to ColumnDescriptor.    * This returns all the keys of the family map which represents the column    * family names of the table.    *    * @return Immutable sorted set of the keys of the families.    */
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|getColumnFamilyNames
parameter_list|()
function_decl|;
comment|/**    * Returns the ColumnDescriptor for a specific column family with name as    * specified by the parameter column.    *    * @param name Column family name    * @return Column descriptor for the passed family name or the family on    * passed in column.    */
name|ColumnFamilyDescriptor
name|getColumnFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|name
parameter_list|)
function_decl|;
comment|/**    * This gets the class associated with the flush policy which determines the    * stores need to be flushed when flushing a region. The class used by default    * is defined in org.apache.hadoop.hbase.regionserver.FlushPolicy.    *    * @return the class name of the flush policy for this table. If this returns    * null, the default flush policy is used.    */
name|String
name|getFlushPolicyClassName
parameter_list|()
function_decl|;
comment|/**    * Returns the maximum size upto which a region can grow to after which a    * region split is triggered. The region size is represented by the size of    * the biggest store file in that region.    *    * @return max hregion size for table, -1 if not set.    */
name|long
name|getMaxFileSize
parameter_list|()
function_decl|;
comment|/**    * Returns the size of the memstore after which a flush to filesystem is    * triggered.    *    * @return memory cache flush size for each hregion, -1 if not set.    */
name|long
name|getMemStoreFlushSize
parameter_list|()
function_decl|;
comment|// TODO: Currently this is used RPC scheduling only. Make it more generic than this; allow it
comment|// to also be priority when scheduling procedures that pertain to this table scheduling first
comment|// those tables with the highest priority (From Yi Liang over on HBASE-18109).
name|int
name|getPriority
parameter_list|()
function_decl|;
comment|/**    * @return Returns the configured replicas per region    */
name|int
name|getRegionReplication
parameter_list|()
function_decl|;
comment|/**    * This gets the class associated with the region split policy which    * determines when a region split should occur. The class used by default is    * defined in org.apache.hadoop.hbase.regionserver.RegionSplitPolicy    *    * @return the class name of the region split policy for this table. If this    * returns null, the default split policy is used.    */
name|String
name|getRegionSplitPolicyClassName
parameter_list|()
function_decl|;
comment|/**    * Get the name of the table    *    * @return TableName    */
name|TableName
name|getTableName
parameter_list|()
function_decl|;
annotation|@
name|Deprecated
name|String
name|getOwnerString
parameter_list|()
function_decl|;
comment|/**    * Getter for accessing the metadata associated with the key.    *    * @param key The key.    * @return A clone value. Null if no mapping for the key    */
name|Bytes
name|getValue
parameter_list|(
name|Bytes
name|key
parameter_list|)
function_decl|;
comment|/**    * Getter for accessing the metadata associated with the key.    *    * @param key The key.    * @return A clone value. Null if no mapping for the key    */
name|byte
index|[]
name|getValue
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
function_decl|;
comment|/**    * Getter for accessing the metadata associated with the key.    *    * @param key The key.    * @return Null if no mapping for the key    */
name|String
name|getValue
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * @return Getter for fetching an unmodifiable map.    */
name|Map
argument_list|<
name|Bytes
argument_list|,
name|Bytes
argument_list|>
name|getValues
parameter_list|()
function_decl|;
comment|/**    * Check if the table has an attached co-processor represented by the name    * className    *    * @param classNameToMatch - Class name of the co-processor    * @return true of the table has a co-processor className    */
name|boolean
name|hasCoprocessor
parameter_list|(
name|String
name|classNameToMatch
parameter_list|)
function_decl|;
comment|/**    * Checks to see if this table contains the given column family    *    * @param name Family name or column name.    * @return true if the table contains the specified family name    */
name|boolean
name|hasColumnFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|name
parameter_list|)
function_decl|;
comment|/**    * @return true if the read-replicas memstore replication is enabled.    */
name|boolean
name|hasRegionMemStoreReplication
parameter_list|()
function_decl|;
comment|/**    * Check if the compaction enable flag of the table is true. If flag is false    * then no minor/major compactions will be done in real.    *    * @return true if table compaction enabled    */
name|boolean
name|isCompactionEnabled
parameter_list|()
function_decl|;
comment|/**    * Checks if this table is<code> hbase:meta</code> region.    *    * @return true if this table is<code> hbase:meta</code> region    */
name|boolean
name|isMetaRegion
parameter_list|()
function_decl|;
comment|/**    * Checks if the table is a<code>hbase:meta</code> table    *    * @return true if table is<code> hbase:meta</code> region.    */
name|boolean
name|isMetaTable
parameter_list|()
function_decl|;
comment|/**    * Check if normalization enable flag of the table is true. If flag is false    * then no region normalizer won't attempt to normalize this table.    *    * @return true if region normalization is enabled for this table    */
name|boolean
name|isNormalizationEnabled
parameter_list|()
function_decl|;
comment|/**    * Check if the readOnly flag of the table is set. If the readOnly flag is set    * then the contents of the table can only be read from but not modified.    *    * @return true if all columns in the table should be read only    */
name|boolean
name|isReadOnly
parameter_list|()
function_decl|;
comment|/**    * Check if the table's cfs' replication scope matched with the replication state    * @param enabled replication state    * @return true if matched, otherwise false    */
specifier|default
name|boolean
name|matchReplicationScope
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|boolean
name|hasEnabled
init|=
literal|false
decl_stmt|;
name|boolean
name|hasDisabled
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ColumnFamilyDescriptor
name|cf
range|:
name|getColumnFamilies
argument_list|()
control|)
block|{
if|if
condition|(
name|cf
operator|.
name|getScope
argument_list|()
operator|!=
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
condition|)
block|{
name|hasDisabled
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|hasEnabled
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|hasEnabled
operator|&&
name|hasDisabled
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|hasEnabled
condition|)
block|{
return|return
name|enabled
return|;
block|}
return|return
operator|!
name|enabled
return|;
block|}
block|}
end_interface

end_unit

