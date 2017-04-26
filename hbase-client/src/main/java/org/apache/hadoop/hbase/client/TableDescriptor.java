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
name|Collection
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
name|HColumnDescriptor
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

begin_comment
comment|/**  * TableDescriptor contains the details about an HBase table such as the descriptors of  * all the column families, is the table a catalog table,<code> -ROOT-</code> or  *<code> hbase:meta</code>, if the table is read only, the maximum size of the memstore,  * when the region split should occur, coprocessors associated with it etc...  */
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
comment|/**    * Returns an array all the {@link HColumnDescriptor} of the column families    * of the table.    *    * @return Array of all the HColumnDescriptors of the current table    *    * @see #getFamilies()    */
name|HColumnDescriptor
index|[]
name|getColumnFamilies
parameter_list|()
function_decl|;
comment|/**    * Returns the count of the column families of the table.    *    * @return Count of column families of the table    */
name|int
name|getColumnFamilyCount
parameter_list|()
function_decl|;
comment|/**    * Getter for fetching an unmodifiable map.    *    * @return an unmodifiable map    */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getConfiguration
parameter_list|()
function_decl|;
comment|/**    * Getter for accessing the configuration value by key    *    * @param key the key whose associated value is to be returned    * @return the value to which the specified key is mapped, or {@code null} if    * this map contains no mapping for the key    */
name|String
name|getConfigurationValue
parameter_list|(
name|String
name|key
parameter_list|)
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
comment|/**    * Returns an unmodifiable collection of all the {@link HColumnDescriptor} of    * all the column families of the table.    *    * @return Immutable collection of {@link HColumnDescriptor} of all the column    * families.    */
name|Collection
argument_list|<
name|HColumnDescriptor
argument_list|>
name|getFamilies
parameter_list|()
function_decl|;
comment|/**    * Returns all the column family names of the current table. The map of    * TableDescriptor contains mapping of family name to HColumnDescriptors.    * This returns all the keys of the family map which represents the column    * family names of the table.    *    * @return Immutable sorted set of the keys of the families.    */
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|getFamiliesKeys
parameter_list|()
function_decl|;
comment|/**    * Returns the HColumnDescriptor for a specific column family with name as    * specified by the parameter column.    *    * @param column Column family name    * @return Column descriptor for the passed family name or the family on    * passed in column.    */
name|HColumnDescriptor
name|getFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|column
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
comment|/**    * Getter for accessing the metadata associated with the key    *    * @param key The key.    * @return The value.    */
name|byte
index|[]
name|getValue
parameter_list|(
name|byte
index|[]
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
comment|/**    * Checks to see if this table contains the given column family    *    * @param familyName Family name or column name.    * @return true if the table contains the specified family name    */
name|boolean
name|hasFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|familyName
parameter_list|)
function_decl|;
comment|/**    * @return true if the read-replicas memstore replication is enabled.    */
name|boolean
name|hasRegionMemstoreReplication
parameter_list|()
function_decl|;
comment|/**    * @return true if there are at least one cf whose replication scope is    * serial.    */
name|boolean
name|hasSerialReplicationScope
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
comment|/**    * Check if the descriptor represents a<code> -ROOT-</code> region.    *    * @return true if this is a<code> -ROOT-</code> region    */
name|boolean
name|isRootRegion
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

