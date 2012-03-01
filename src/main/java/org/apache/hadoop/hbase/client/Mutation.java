begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Bytes
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
block|{
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
name|long
name|lockId
init|=
operator|-
literal|1L
decl_stmt|;
specifier|protected
name|boolean
name|writeToWAL
init|=
literal|true
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
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
name|KeyValue
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
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
name|KeyValue
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
name|KeyValue
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
comment|// map from this family to details for each kv affected within the family
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
comment|// add details for each kv
for|for
control|(
name|KeyValue
name|kv
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
return|return
name|map
return|;
block|}
comment|/**    * @return true if edits should be applied to WAL, false if not    */
specifier|public
name|boolean
name|getWriteToWAL
parameter_list|()
block|{
return|return
name|this
operator|.
name|writeToWAL
return|;
block|}
comment|/**    * Set whether this Delete should be written to the WAL or not.    * Not writing the WAL means you may lose edits on server crash.    * @param write true if edits should be written to WAL, false if not    */
specifier|public
name|void
name|setWriteToWAL
parameter_list|(
name|boolean
name|write
parameter_list|)
block|{
name|this
operator|.
name|writeToWAL
operator|=
name|write
expr_stmt|;
block|}
comment|/**    * Method for retrieving the put's familyMap    * @return familyMap    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|getFamilyMap
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
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|map
parameter_list|)
block|{
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
comment|/**    * Method for retrieving the delete's RowLock    * @return RowLock    */
specifier|public
name|RowLock
name|getRowLock
parameter_list|()
block|{
return|return
operator|new
name|RowLock
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|this
operator|.
name|lockId
argument_list|)
return|;
block|}
comment|/**    * Method for retrieving the delete's lock ID.    *    * @return The lock ID.    */
specifier|public
name|long
name|getLockId
parameter_list|()
block|{
return|return
name|this
operator|.
name|lockId
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
comment|/**    * @return the total number of KeyValues    */
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
name|KeyValue
argument_list|>
name|kvList
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
name|kvList
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
block|}
end_class

end_unit

