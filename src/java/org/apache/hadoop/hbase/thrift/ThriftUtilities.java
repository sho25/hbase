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
name|HColumnDescriptor
operator|.
name|CompressionType
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
name|io
operator|.
name|RowResult
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
comment|/**    * This utility method creates a new Hbase HColumnDescriptor object based on a    * Thrift ColumnDescriptor "struct".    *     * @param in    *          Thrift ColumnDescriptor object    * @return HColumnDescriptor    * @throws IllegalArgument    */
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
name|CompressionType
name|comp
init|=
name|CompressionType
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|compression
argument_list|)
decl_stmt|;
name|boolean
name|bloom
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|bloomFilterType
operator|.
name|compareTo
argument_list|(
literal|"NONE"
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|bloom
operator|=
literal|true
expr_stmt|;
block|}
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
name|HColumnDescriptor
name|col
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|in
operator|.
name|name
argument_list|,
name|in
operator|.
name|maxVersions
argument_list|,
name|comp
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
name|maxValueLength
argument_list|,
name|in
operator|.
name|timeToLive
argument_list|,
name|bloom
argument_list|)
decl_stmt|;
return|return
name|col
return|;
block|}
comment|/**    * This utility method creates a new Thrift ColumnDescriptor "struct" based on    * an Hbase HColumnDescriptor object.    *     * @param in    *          Hbase HColumnDescriptor object    * @return Thrift ColumnDescriptor    */
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
name|in
operator|.
name|getName
argument_list|()
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
name|maxValueLength
operator|=
name|in
operator|.
name|getMaxValueLength
argument_list|()
expr_stmt|;
name|col
operator|.
name|bloomFilterType
operator|=
name|Boolean
operator|.
name|toString
argument_list|(
name|in
operator|.
name|isBloomfilter
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|col
return|;
block|}
comment|/**    * This utility method creates a new Thrift TCell "struct" based on    * an Hbase Cell object.    *     * @param in    *          Hbase Cell object    * @return Thrift TCell    */
specifier|static
specifier|public
name|TCell
name|cellFromHBase
parameter_list|(
name|Cell
name|in
parameter_list|)
block|{
return|return
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
return|;
block|}
comment|/**    * This utility method creates a new Thrift TRowResult "struct" based on    * an Hbase RowResult object.    *     * @param in    *          Hbase RowResult object    * @return Thrift TRowResult    */
specifier|static
specifier|public
name|TRowResult
name|rowResultFromHBase
parameter_list|(
name|RowResult
name|in
parameter_list|)
block|{
name|TRowResult
name|result
init|=
operator|new
name|TRowResult
argument_list|()
decl_stmt|;
if|if
condition|(
name|in
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|result
operator|.
name|row
operator|=
name|in
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
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|entry
range|:
name|in
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
operator|.
name|columns
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|ThriftUtilities
operator|.
name|cellFromHBase
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

