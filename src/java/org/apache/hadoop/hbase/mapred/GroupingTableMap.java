begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
package|;
end_package

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
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|regionserver
operator|.
name|HStoreKey
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
name|ImmutableBytesWritable
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
name|MapWritable
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
name|Text
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|OutputCollector
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
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * Extract grouping columns from input record  */
end_comment

begin_class
specifier|public
class|class
name|GroupingTableMap
extends|extends
name|TableMap
argument_list|<
name|Text
argument_list|,
name|MapWritable
argument_list|>
block|{
comment|/**    * JobConf parameter to specify the columns used to produce the key passed to     * collect from the map phase    */
specifier|public
specifier|static
specifier|final
name|String
name|GROUP_COLUMNS
init|=
literal|"hbase.mapred.groupingtablemap.columns"
decl_stmt|;
specifier|protected
name|Text
index|[]
name|m_columns
decl_stmt|;
comment|/**    * Use this before submitting a TableMap job. It will appropriately set up the    * JobConf.    *    * @param table table to be processed    * @param columns space separated list of columns to fetch    * @param groupColumns space separated list of columns used to form the key    * used in collect    * @param mapper map class    * @param job job configuration object    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|void
name|initJob
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|groupColumns
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableMap
argument_list|>
name|mapper
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|initJob
argument_list|(
name|table
argument_list|,
name|columns
argument_list|,
name|mapper
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|GROUP_COLUMNS
argument_list|,
name|groupColumns
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|super
operator|.
name|configure
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|String
index|[]
name|cols
init|=
name|job
operator|.
name|get
argument_list|(
name|GROUP_COLUMNS
argument_list|,
literal|""
argument_list|)
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|m_columns
operator|=
operator|new
name|Text
index|[
name|cols
operator|.
name|length
index|]
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
name|cols
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|m_columns
index|[
name|i
index|]
operator|=
operator|new
name|Text
argument_list|(
name|cols
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Extract the grouping columns from value to construct a new key.    *     * Pass the new key and value to reduce.    * If any of the grouping columns are not found in the value, the record is skipped.    *    * @see org.apache.hadoop.hbase.mapred.TableMap#map(org.apache.hadoop.hbase.regionserver.HStoreKey, org.apache.hadoop.io.MapWritable, org.apache.hadoop.mapred.OutputCollector, org.apache.hadoop.mapred.Reporter)    */
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|HStoreKey
name|key
parameter_list|,
name|MapWritable
name|value
parameter_list|,
name|OutputCollector
argument_list|<
name|Text
argument_list|,
name|MapWritable
argument_list|>
name|output
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|keyVals
init|=
name|extractKeyValues
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyVals
operator|!=
literal|null
condition|)
block|{
name|Text
name|tKey
init|=
name|createGroupKey
argument_list|(
name|keyVals
argument_list|)
decl_stmt|;
name|output
operator|.
name|collect
argument_list|(
name|tKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Extract columns values from the current record. This method returns    * null if any of the columns are not found.    *     * Override this method if you want to deal with nulls differently.    *     * @param r    * @return array of byte values    */
specifier|protected
name|byte
index|[]
index|[]
name|extractKeyValues
parameter_list|(
name|MapWritable
name|r
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|keyVals
init|=
literal|null
decl_stmt|;
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|foundList
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numCols
init|=
name|m_columns
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|numCols
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Writable
argument_list|,
name|Writable
argument_list|>
name|e
range|:
name|r
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Text
name|column
init|=
operator|(
name|Text
operator|)
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numCols
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|column
operator|.
name|equals
argument_list|(
name|m_columns
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|foundList
operator|.
name|add
argument_list|(
operator|(
operator|(
name|ImmutableBytesWritable
operator|)
name|e
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|foundList
operator|.
name|size
argument_list|()
operator|==
name|numCols
condition|)
block|{
name|keyVals
operator|=
name|foundList
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
name|numCols
index|]
index|[]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|keyVals
return|;
block|}
comment|/**    * Create a key by concatenating multiple column values.     * Override this function in order to produce different types of keys.    *     * @param vals    * @return key generated by concatenating multiple column values    */
specifier|protected
name|Text
name|createGroupKey
parameter_list|(
name|byte
index|[]
index|[]
name|vals
parameter_list|)
block|{
if|if
condition|(
name|vals
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
operator|new
name|String
argument_list|(
name|vals
index|[
name|i
index|]
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|Text
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

