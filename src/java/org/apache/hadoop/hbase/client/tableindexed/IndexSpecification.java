begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|tableindexed
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HBaseConfiguration
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
name|io
operator|.
name|ObjectWritable
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/** Holds the specification for a single secondary index. */
end_comment

begin_class
specifier|public
class|class
name|IndexSpecification
implements|implements
name|Writable
block|{
comment|// Columns that are indexed (part of the indexRowKey)
specifier|private
name|byte
index|[]
index|[]
name|indexedColumns
decl_stmt|;
comment|// Constructs the
specifier|private
name|IndexKeyGenerator
name|keyGenerator
decl_stmt|;
comment|// Additional columns mapped into the indexed row. These will be available for
comment|// filters when scanning the index.
specifier|private
name|byte
index|[]
index|[]
name|additionalColumns
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|allColumns
decl_stmt|;
comment|// Id of this index, unique within a table.
specifier|private
name|String
name|indexId
decl_stmt|;
comment|/** Construct an "simple" index spec for a single column. */
specifier|public
name|IndexSpecification
parameter_list|(
name|String
name|indexId
parameter_list|,
name|byte
index|[]
name|indexedColumn
parameter_list|)
block|{
name|this
argument_list|(
name|indexId
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|indexedColumn
block|}
argument_list|,
literal|null
argument_list|,
operator|new
name|SimpleIndexKeyGenerator
argument_list|(
name|indexedColumn
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct an index spec by specifying everything.    *     * @param indexId    * @param indexedColumns    * @param additionalColumns    * @param keyGenerator    */
specifier|public
name|IndexSpecification
parameter_list|(
name|String
name|indexId
parameter_list|,
name|byte
index|[]
index|[]
name|indexedColumns
parameter_list|,
name|byte
index|[]
index|[]
name|additionalColumns
parameter_list|,
name|IndexKeyGenerator
name|keyGenerator
parameter_list|)
block|{
name|this
operator|.
name|indexId
operator|=
name|indexId
expr_stmt|;
name|this
operator|.
name|indexedColumns
operator|=
name|indexedColumns
expr_stmt|;
name|this
operator|.
name|additionalColumns
operator|=
name|additionalColumns
expr_stmt|;
name|this
operator|.
name|keyGenerator
operator|=
name|keyGenerator
expr_stmt|;
name|this
operator|.
name|makeAllColumns
argument_list|()
expr_stmt|;
block|}
specifier|public
name|IndexSpecification
parameter_list|()
block|{
comment|// For writable
block|}
specifier|private
name|void
name|makeAllColumns
parameter_list|()
block|{
name|this
operator|.
name|allColumns
operator|=
operator|new
name|byte
index|[
name|indexedColumns
operator|.
name|length
operator|+
operator|(
name|additionalColumns
operator|==
literal|null
condition|?
literal|0
else|:
name|additionalColumns
operator|.
name|length
operator|)
index|]
index|[]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|indexedColumns
argument_list|,
literal|0
argument_list|,
name|allColumns
argument_list|,
literal|0
argument_list|,
name|indexedColumns
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|additionalColumns
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|additionalColumns
argument_list|,
literal|0
argument_list|,
name|allColumns
argument_list|,
name|indexedColumns
operator|.
name|length
argument_list|,
name|additionalColumns
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the indexedColumns.    *     * @return Return the indexedColumns.    */
specifier|public
name|byte
index|[]
index|[]
name|getIndexedColumns
parameter_list|()
block|{
return|return
name|indexedColumns
return|;
block|}
comment|/**    * Get the keyGenerator.    *     * @return Return the keyGenerator.    */
specifier|public
name|IndexKeyGenerator
name|getKeyGenerator
parameter_list|()
block|{
return|return
name|keyGenerator
return|;
block|}
comment|/**    * Get the additionalColumns.    *     * @return Return the additionalColumns.    */
specifier|public
name|byte
index|[]
index|[]
name|getAdditionalColumns
parameter_list|()
block|{
return|return
name|additionalColumns
return|;
block|}
comment|/**    * Get the indexId.    *     * @return Return the indexId.    */
specifier|public
name|String
name|getIndexId
parameter_list|()
block|{
return|return
name|indexId
return|;
block|}
specifier|public
name|byte
index|[]
index|[]
name|getAllColumns
parameter_list|()
block|{
return|return
name|allColumns
return|;
block|}
specifier|public
name|boolean
name|containsColumn
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
for|for
control|(
name|byte
index|[]
name|col
range|:
name|allColumns
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|col
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|byte
index|[]
name|getIndexedTableName
parameter_list|(
name|byte
index|[]
name|baseTableName
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|add
argument_list|(
name|baseTableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-"
operator|+
name|indexId
argument_list|)
argument_list|)
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|indexId
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|int
name|numIndexedCols
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|indexedColumns
operator|=
operator|new
name|byte
index|[
name|numIndexedCols
index|]
index|[]
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
name|numIndexedCols
condition|;
name|i
operator|++
control|)
block|{
name|indexedColumns
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|int
name|numAdditionalCols
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|additionalColumns
operator|=
operator|new
name|byte
index|[
name|numAdditionalCols
index|]
index|[]
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
name|numAdditionalCols
condition|;
name|i
operator|++
control|)
block|{
name|additionalColumns
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|makeAllColumns
argument_list|()
expr_stmt|;
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|keyGenerator
operator|=
operator|(
name|IndexKeyGenerator
operator|)
name|ObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|indexId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|indexedColumns
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|indexedColumns
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|additionalColumns
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|additionalColumns
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|col
range|:
name|additionalColumns
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|ObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|keyGenerator
argument_list|,
name|IndexKeyGenerator
operator|.
name|class
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

