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
name|hfile
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
name|StoreFile
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
name|StoreFile
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
name|StoreFile
operator|.
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
name|in
operator|.
name|name
operator|.
name|length
operator|<=
literal|0
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
name|in
operator|.
name|name
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
argument_list|,
name|in
operator|.
name|maxVersions
argument_list|,
name|comp
operator|.
name|getName
argument_list|()
argument_list|,
name|in
operator|.
name|inMemory
argument_list|,
name|in
operator|.
name|blockCacheEnabled
argument_list|,
name|in
operator|.
name|timeToLive
argument_list|,
name|bt
operator|.
name|toString
argument_list|()
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
name|KeyValue
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
name|in
operator|.
name|getValue
argument_list|()
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
name|KeyValue
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
name|in
index|[
name|i
index|]
operator|.
name|getValue
argument_list|()
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
comment|/**    * This utility method creates a list of Thrift TRowResult "struct" based on    * an Hbase RowResult object. The empty list is returned if the input is    * null.    *    * @param in    *          Hbase RowResult object    * @return Thrift TRowResult array    */
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
name|result_
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|result
operator|.
name|columns
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|TCell
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|result_
operator|.
name|sorted
argument_list|()
control|)
block|{
name|result
operator|.
name|columns
operator|.
name|put
argument_list|(
name|KeyValue
operator|.
name|makeColumn
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|,
operator|new
name|TCell
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

