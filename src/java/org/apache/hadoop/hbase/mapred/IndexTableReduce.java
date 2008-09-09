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

begin_comment
comment|//TODO: remove
end_comment

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
name|MapReduceBase
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
name|Reducer
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
import|;
end_import

begin_comment
comment|/**  * Construct a Lucene document per row, which is consumed by IndexOutputFormat  * to build a Lucene index  */
end_comment

begin_class
specifier|public
class|class
name|IndexTableReduce
extends|extends
name|MapReduceBase
implements|implements
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|RowResult
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|LuceneDocumentWrapper
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|IndexTableReduce
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|IndexConfiguration
name|indexConf
decl_stmt|;
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
name|indexConf
operator|=
operator|new
name|IndexConfiguration
argument_list|()
expr_stmt|;
name|String
name|content
init|=
name|job
operator|.
name|get
argument_list|(
literal|"hbase.index.conf"
argument_list|)
decl_stmt|;
if|if
condition|(
name|content
operator|!=
literal|null
condition|)
block|{
name|indexConf
operator|.
name|addFromXML
argument_list|(
name|content
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Index conf: "
operator|+
name|indexConf
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|reduce
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Iterator
argument_list|<
name|RowResult
argument_list|>
name|values
parameter_list|,
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|LuceneDocumentWrapper
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
if|if
condition|(
operator|!
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return;
block|}
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
comment|// index and store row key, row key already UTF-8 encoded
name|Field
name|keyField
init|=
operator|new
name|Field
argument_list|(
name|indexConf
operator|.
name|getRowkeyName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|,
name|Field
operator|.
name|Index
operator|.
name|UN_TOKENIZED
argument_list|)
decl_stmt|;
name|keyField
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|keyField
argument_list|)
expr_stmt|;
while|while
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|RowResult
name|value
init|=
name|values
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// each column (name-value pair) is a field (name-value pair)
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
name|value
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// name is already UTF-8 encoded
name|String
name|column
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|columnValue
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Field
operator|.
name|Store
name|store
init|=
name|indexConf
operator|.
name|isStore
argument_list|(
name|column
argument_list|)
condition|?
name|Field
operator|.
name|Store
operator|.
name|YES
else|:
name|Field
operator|.
name|Store
operator|.
name|NO
decl_stmt|;
name|Field
operator|.
name|Index
name|index
init|=
name|indexConf
operator|.
name|isIndex
argument_list|(
name|column
argument_list|)
condition|?
operator|(
name|indexConf
operator|.
name|isTokenize
argument_list|(
name|column
argument_list|)
condition|?
name|Field
operator|.
name|Index
operator|.
name|TOKENIZED
else|:
name|Field
operator|.
name|Index
operator|.
name|UN_TOKENIZED
operator|)
else|:
name|Field
operator|.
name|Index
operator|.
name|NO
decl_stmt|;
comment|// UTF-8 encode value
name|Field
name|field
init|=
operator|new
name|Field
argument_list|(
name|column
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|columnValue
argument_list|)
argument_list|,
name|store
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|field
operator|.
name|setBoost
argument_list|(
name|indexConf
operator|.
name|getBoost
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
name|field
operator|.
name|setOmitNorms
argument_list|(
name|indexConf
operator|.
name|isOmitNorms
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
name|output
operator|.
name|collect
argument_list|(
name|key
argument_list|,
operator|new
name|LuceneDocumentWrapper
argument_list|(
name|doc
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

