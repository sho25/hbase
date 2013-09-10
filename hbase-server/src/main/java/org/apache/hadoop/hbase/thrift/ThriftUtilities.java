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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|List
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
name|client
operator|.
name|Increment
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
name|io
operator|.
name|compress
operator|.
name|Compression
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
name|regionserver
operator|.
name|BloomType
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
name|thrift
operator|.
name|generated
operator|.
name|ColumnDescriptor
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
name|thrift
operator|.
name|generated
operator|.
name|IllegalArgument
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
name|thrift
operator|.
name|generated
operator|.
name|TCell
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
name|thrift
operator|.
name|generated
operator|.
name|TColumn
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
name|thrift
operator|.
name|generated
operator|.
name|TIncrement
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
name|thrift
operator|.
name|generated
operator|.
name|TRowResult
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
name|Private
specifier|public
class|class
name|ThriftUtilities
block|{
comment|/**    * This utility method creates a new Hbase HColumnDescriptor object based on a    * Thrift ColumnDescriptor "struct".    *    * @param in    *          Thrift ColumnDescriptor object    * @return HColumnDescriptor    * @throws IllegalArgument    */
specifier|static
specifier|public
name|HColumnDescriptor
name|colDescFromThrift
parameter_list|(
name|ColumnDescriptor
name|in
parameter_list|)
throws|throws
name|IllegalArgument
block|{
name|Compression
operator|.
name|Algorithm
name|comp
init|=
name|Compression
operator|.
name|getCompressionAlgorithmByName
argument_list|(
name|in
operator|.
name|compression
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
name|BloomType
name|bt
init|=
name|BloomType
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|bloomFilterType
argument_list|)
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|name
operator|==
literal|null
operator|||
operator|!
name|in
operator|.
name|name
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgument
argument_list|(
literal|"column name is empty"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|parsedName
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|Bytes
operator|.
name|getBytes
argument_list|(
name|in
operator|.
name|name
argument_list|)
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|HColumnDescriptor
name|col
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|parsedName
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|in
operator|.
name|maxVersions
argument_list|)
operator|.
name|setCompressionType
argument_list|(
name|comp
argument_list|)
operator|.
name|setInMemory
argument_list|(
name|in
operator|.
name|inMemory
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
name|in
operator|.
name|blockCacheEnabled
argument_list|)
operator|.
name|setTimeToLive
argument_list|(
name|in
operator|.
name|timeToLive
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|bt
argument_list|)
decl_stmt|;
return|return
name|col
return|;
block|}
comment|/**    * This utility method creates a new Thrift ColumnDescriptor "struct" based on    * an Hbase HColumnDescriptor object.    *    * @param in    *          Hbase HColumnDescriptor object    * @return Thrift ColumnDescriptor    */
specifier|static
specifier|public
name|ColumnDescriptor
name|colDescFromHbase
parameter_list|(
name|HColumnDescriptor
name|in
parameter_list|)
block|{
name|ColumnDescriptor
name|col
init|=
operator|new
name|ColumnDescriptor
argument_list|()
decl_stmt|;
name|col
operator|.
name|name
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|in
operator|.
name|getName
argument_list|()
argument_list|,
name|KeyValue
operator|.
name|COLUMN_FAMILY_DELIM_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
name|col
operator|.
name|maxVersions
operator|=
name|in
operator|.
name|getMaxVersions
argument_list|()
expr_stmt|;
name|col
operator|.
name|compression
operator|=
name|in
operator|.
name|getCompression
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|col
operator|.
name|inMemory
operator|=
name|in
operator|.
name|isInMemory
argument_list|()
expr_stmt|;
name|col
operator|.
name|blockCacheEnabled
operator|=
name|in
operator|.
name|isBlockCacheEnabled
argument_list|()
expr_stmt|;
name|col
operator|.
name|bloomFilterType
operator|=
name|in
operator|.
name|getBloomFilterType
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
return|return
name|col
return|;
block|}
comment|/**    * This utility method creates a list of Thrift TCell "struct" based on    * an Hbase Cell object. The empty list is returned if the input is null.    *    * @param in    *          Hbase Cell object    * @return Thrift TCell array    */
specifier|static
specifier|public
name|List
argument_list|<
name|TCell
argument_list|>
name|cellFromHBase
parameter_list|(
name|Cell
name|in
parameter_list|)
block|{
name|List
argument_list|<
name|TCell
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|TCell
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|TCell
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|in
argument_list|)
argument_list|)
argument_list|,
name|in
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
comment|/**    * This utility method creates a list of Thrift TCell "struct" based on    * an Hbase Cell array. The empty list is returned if the input is null.    * @param in Hbase Cell array    * @return Thrift TCell array    */
specifier|static
specifier|public
name|List
argument_list|<
name|TCell
argument_list|>
name|cellFromHBase
parameter_list|(
name|Cell
index|[]
name|in
parameter_list|)
block|{
name|List
argument_list|<
name|TCell
argument_list|>
name|list
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|TCell
argument_list|>
argument_list|(
name|in
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|in
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|TCell
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|in
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|,
name|in
index|[
name|i
index|]
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|TCell
argument_list|>
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
comment|/**    * This utility method creates a list of Thrift TRowResult "struct" based on    * an Hbase RowResult object. The empty list is returned if the input is    * null.    *    * @param in    *          Hbase RowResult object    * @param sortColumns    *          This boolean dictates if row data is returned in a sorted order    *          sortColumns = True will set TRowResult's sortedColumns member    *                        which is an ArrayList of TColumn struct    *          sortColumns = False will set TRowResult's columns member which is    *                        a map of columnName and TCell struct    * @return Thrift TRowResult array    */
specifier|static
specifier|public
name|List
argument_list|<
name|TRowResult
argument_list|>
name|rowResultFromHBase
parameter_list|(
name|Result
index|[]
name|in
parameter_list|,
name|boolean
name|sortColumns
parameter_list|)
block|{
name|List
argument_list|<
name|TRowResult
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|TRowResult
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|result_
range|:
name|in
control|)
block|{
if|if
condition|(
name|result_
operator|==
literal|null
operator|||
name|result_
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|TRowResult
name|result
init|=
operator|new
name|TRowResult
argument_list|()
decl_stmt|;
name|result
operator|.
name|row
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|result_
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|sortColumns
condition|)
block|{
name|result
operator|.
name|sortedColumns
operator|=
operator|new
name|ArrayList
argument_list|<
name|TColumn
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|result_
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|result
operator|.
name|sortedColumns
operator|.
name|add
argument_list|(
operator|new
name|TColumn
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|KeyValue
operator|.
name|makeColumn
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|kv
argument_list|)
argument_list|,
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
argument_list|,
operator|new
name|TCell
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|result
operator|.
name|columns
operator|=
operator|new
name|TreeMap
argument_list|<
name|ByteBuffer
argument_list|,
name|TCell
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|result_
operator|.
name|rawCells
argument_list|()
control|)
block|{
name|result
operator|.
name|columns
operator|.
name|put
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|KeyValue
operator|.
name|makeColumn
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|kv
argument_list|)
argument_list|,
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|)
argument_list|,
operator|new
name|TCell
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
argument_list|)
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
comment|/**    * This utility method creates a list of Thrift TRowResult "struct" based on    * an array of Hbase RowResult objects. The empty list is returned if the input is    * null.    *    * @param in    *          Array of Hbase RowResult objects    * @return Thrift TRowResult array    */
specifier|static
specifier|public
name|List
argument_list|<
name|TRowResult
argument_list|>
name|rowResultFromHBase
parameter_list|(
name|Result
index|[]
name|in
parameter_list|)
block|{
return|return
name|rowResultFromHBase
argument_list|(
name|in
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|static
specifier|public
name|List
argument_list|<
name|TRowResult
argument_list|>
name|rowResultFromHBase
parameter_list|(
name|Result
name|in
parameter_list|)
block|{
name|Result
index|[]
name|result
init|=
block|{
name|in
block|}
decl_stmt|;
return|return
name|rowResultFromHBase
argument_list|(
name|result
argument_list|)
return|;
block|}
comment|/**    * From a {@link TIncrement} create an {@link Increment}.    * @param tincrement the Thrift version of an increment    * @return an increment that the {@link TIncrement} represented.    */
specifier|public
specifier|static
name|Increment
name|incrementFromThrift
parameter_list|(
name|TIncrement
name|tincrement
parameter_list|)
block|{
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|tincrement
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|famAndQf
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|tincrement
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|famAndQf
operator|.
name|length
operator|<
literal|1
condition|)
return|return
literal|null
return|;
name|byte
index|[]
name|qual
init|=
name|famAndQf
operator|.
name|length
operator|==
literal|1
condition|?
operator|new
name|byte
index|[
literal|0
index|]
else|:
name|famAndQf
index|[
literal|1
index|]
decl_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|qual
argument_list|,
name|tincrement
operator|.
name|getAmmount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|inc
return|;
block|}
block|}
end_class

end_unit

