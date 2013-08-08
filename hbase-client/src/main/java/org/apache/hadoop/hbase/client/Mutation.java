begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|Cell
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
name|CellScannable
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
name|CellScanner
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
name|CellUtil
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
name|KeyValueUtil
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
name|HeapSize
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
name|ClassSize
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|Mutation
extends|extends
name|OperationWithAttributes
implements|implements
name|Row
implements|,
name|CellScannable
implements|,
name|HeapSize
block|{
specifier|public
specifier|static
specifier|final
name|long
name|MUTATION_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
comment|// This
name|ClassSize
operator|.
name|OBJECT
operator|+
comment|// row + OperationWithAttributes.attributes
literal|2
operator|*
name|ClassSize
operator|.
name|REFERENCE
operator|+
comment|// Timestamp
literal|1
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
operator|+
comment|// durability
name|ClassSize
operator|.
name|REFERENCE
operator|+
comment|// familyMap
name|ClassSize
operator|.
name|REFERENCE
operator|+
comment|// familyMap
name|ClassSize
operator|.
name|TREEMAP
argument_list|)
decl_stmt|;
comment|// Attribute used in Mutations to indicate the originating cluster.
specifier|private
specifier|static
specifier|final
name|String
name|CLUSTER_ID_ATTR
init|=
literal|"_c.id_"
decl_stmt|;
specifier|protected
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|protected
name|long
name|ts
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
specifier|protected
name|Durability
name|durability
init|=
name|Durability
operator|.
name|USE_DEFAULT
decl_stmt|;
comment|// A Map sorted by column family.
specifier|protected
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|familyMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|CellScanner
name|cellScanner
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|getFamilyCellMap
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Creates an empty list if one doesn't exist for the given column family    * or else it returns the associated list of Cell objects.    *    * @param family column family    * @return a list of Cell objects, returns an empty list if one doesn't exist.    */
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|getCellList
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|list
init|=
name|this
operator|.
name|familyMap
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
comment|/*    * Create a nnnnnnnn with this objects row key and the Put identifier.    *    * @return a KeyValue with this objects row key and the Put identifier.    */
name|KeyValue
name|createPutKeyValue
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|ts
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
operator|new
name|KeyValue
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|ts
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Compile the column family (i.e. schema) information    * into a Map. Useful for parsing and aggregation by debugging,    * logging, and administration tools.    * @return Map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getFingerprint
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|families
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// ideally, we would also include table information, but that information
comment|// is not stored in each Operation instance.
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|families
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|families
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
comment|/**    * Compile the details beyond the scope of getFingerprint (row, columns,    * timestamps, etc.) into a Map along with the fingerprinted information.    * Useful for debugging, logging, and administration tools.    * @param maxCols a limit on the number of columns output prior to truncation    * @return Map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|(
name|int
name|maxCols
parameter_list|)
block|{
comment|// we start with the fingerprint map and build on top of it.
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|getFingerprint
argument_list|()
decl_stmt|;
comment|// replace the fingerprint's simple list of families with a
comment|// map from column families to lists of qualifiers and kv details
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|>
name|columns
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|columns
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"row"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|colCount
init|=
literal|0
decl_stmt|;
comment|// iterate through all column families affected
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// map from this family to details for each cell affected within the family
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|qualifierDetails
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|columns
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|qualifierDetails
argument_list|)
expr_stmt|;
name|colCount
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|maxCols
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// add details for each cell
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
operator|--
name|maxCols
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// KeyValue v1 expectation.  Cast for now until we go all Cell all the time.
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|kvMap
init|=
name|kv
operator|.
name|toStringMap
argument_list|()
decl_stmt|;
comment|// row and family information are already available in the bigger map
name|kvMap
operator|.
name|remove
argument_list|(
literal|"row"
argument_list|)
expr_stmt|;
name|kvMap
operator|.
name|remove
argument_list|(
literal|"family"
argument_list|)
expr_stmt|;
name|qualifierDetails
operator|.
name|add
argument_list|(
name|kvMap
argument_list|)
expr_stmt|;
block|}
block|}
name|map
operator|.
name|put
argument_list|(
literal|"totalColumns"
argument_list|,
name|colCount
argument_list|)
expr_stmt|;
comment|// add the id if set
if|if
condition|(
name|getId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|map
operator|.
name|put
argument_list|(
literal|"id"
argument_list|,
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
comment|/**    * Set the durability for this mutation    * @param d    */
specifier|public
name|void
name|setDurability
parameter_list|(
name|Durability
name|d
parameter_list|)
block|{
name|this
operator|.
name|durability
operator|=
name|d
expr_stmt|;
block|}
comment|/** Get the current durability */
specifier|public
name|Durability
name|getDurability
parameter_list|()
block|{
return|return
name|this
operator|.
name|durability
return|;
block|}
comment|/**    * Method for retrieving the put's familyMap    * @return familyMap    */
specifier|public
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|getFamilyCellMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|familyMap
return|;
block|}
comment|/**    * Method for setting the put's familyMap    */
specifier|public
name|void
name|setFamilyMap
parameter_list|(
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|map
parameter_list|)
block|{
comment|// TODO: Shut this down or move it up to be a Constructor.  Get new object rather than change
comment|// this internal data member.
name|this
operator|.
name|familyMap
operator|=
name|map
expr_stmt|;
block|}
comment|/**    * Method to check if the familyMap is empty    * @return true if empty, false otherwise    */
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|familyMap
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * Method for retrieving the delete's row    * @return row    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|Row
name|d
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|getRow
argument_list|()
argument_list|,
name|d
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Method for retrieving the timestamp    * @return timestamp    */
specifier|public
name|long
name|getTimeStamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|ts
return|;
block|}
comment|/**    * Set the replication custer id.    * @param clusterId    */
specifier|public
name|void
name|setClusterId
parameter_list|(
name|UUID
name|clusterId
parameter_list|)
block|{
if|if
condition|(
name|clusterId
operator|==
literal|null
condition|)
return|return;
name|byte
index|[]
name|val
init|=
operator|new
name|byte
index|[
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|Bytes
operator|.
name|putLong
argument_list|(
name|val
argument_list|,
literal|0
argument_list|,
name|clusterId
operator|.
name|getMostSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|putLong
argument_list|(
name|val
argument_list|,
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|,
name|clusterId
operator|.
name|getLeastSignificantBits
argument_list|()
argument_list|)
expr_stmt|;
name|setAttribute
argument_list|(
name|CLUSTER_ID_ATTR
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return The replication cluster id.    */
specifier|public
name|UUID
name|getClusterId
parameter_list|()
block|{
name|byte
index|[]
name|attr
init|=
name|getAttribute
argument_list|(
name|CLUSTER_ID_ATTR
argument_list|)
decl_stmt|;
if|if
condition|(
name|attr
operator|==
literal|null
condition|)
block|{
return|return
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
return|;
block|}
return|return
operator|new
name|UUID
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|attr
argument_list|,
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|attr
argument_list|,
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Number of KeyValues carried by this Mutation.    * @return the total number of KeyValues    */
specifier|public
name|int
name|size
parameter_list|()
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|cells
range|:
name|this
operator|.
name|familyMap
operator|.
name|values
argument_list|()
control|)
block|{
name|size
operator|+=
name|cells
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
comment|/**    * @return the number of different families    */
specifier|public
name|int
name|numFamilies
parameter_list|()
block|{
return|return
name|familyMap
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return Calculate what Mutation adds to class heap size.    */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
name|long
name|heapsize
init|=
name|MUTATION_OVERHEAD
decl_stmt|;
comment|// Adding row
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|this
operator|.
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Adding map overhead
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|this
operator|.
name|familyMap
operator|.
name|size
argument_list|()
operator|*
name|ClassSize
operator|.
name|MAP_ENTRY
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|//Adding key overhead
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|//This part is kinds tricky since the JVM can reuse references if you
comment|//store the same value, but have a good match with SizeOf at the moment
comment|//Adding value overhead
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAYLIST
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|heapsize
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|ARRAY
operator|+
name|size
operator|*
name|ClassSize
operator|.
name|REFERENCE
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|heapsize
operator|+=
name|kv
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
block|}
name|heapsize
operator|+=
name|getAttributeSize
argument_list|()
expr_stmt|;
name|heapsize
operator|+=
name|extraHeapSize
argument_list|()
expr_stmt|;
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|heapsize
argument_list|)
return|;
block|}
comment|/**    * Subclasses should override this method to add the heap size of their own fields.    * @return the heap size to add (will be aligned).    */
specifier|protected
name|long
name|extraHeapSize
parameter_list|()
block|{
return|return
literal|0L
return|;
block|}
comment|/**    * @param row Row to check    * @throws IllegalArgumentException Thrown if<code>row</code> is empty or null or    *&gt; {@link HConstants#MAX_ROW_LENGTH}    * @return<code>row</code>    */
specifier|static
name|byte
index|[]
name|checkRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
name|checkRow
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|==
literal|null
condition|?
literal|0
else|:
name|row
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * @param row Row to check    * @param offset    * @param length    * @throws IllegalArgumentException Thrown if<code>row</code> is empty or null or    *&gt; {@link HConstants#MAX_ROW_LENGTH}    * @return<code>row</code>    */
specifier|static
name|byte
index|[]
name|checkRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Row buffer is null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Row length is 0"
argument_list|)
throw|;
block|}
if|if
condition|(
name|length
operator|>
name|HConstants
operator|.
name|MAX_ROW_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Row length "
operator|+
name|length
operator|+
literal|" is> "
operator|+
name|HConstants
operator|.
name|MAX_ROW_LENGTH
argument_list|)
throw|;
block|}
return|return
name|row
return|;
block|}
block|}
end_class

end_unit

