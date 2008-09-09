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
name|Random
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|FileOutputFormat
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
name|RecordWriter
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
name|hadoop
operator|.
name|util
operator|.
name|Progressable
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
name|analysis
operator|.
name|Analyzer
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
name|index
operator|.
name|IndexWriter
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
name|search
operator|.
name|Similarity
import|;
end_import

begin_comment
comment|/**  * Create a local index, unwrap Lucene documents created by reduce, add them to  * the index, and copy the index to the destination.  */
end_comment

begin_class
specifier|public
class|class
name|IndexOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|LuceneDocumentWrapper
argument_list|>
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IndexOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|LuceneDocumentWrapper
argument_list|>
name|getRecordWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|String
name|name
parameter_list|,
specifier|final
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|perm
init|=
operator|new
name|Path
argument_list|(
name|FileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|job
argument_list|)
argument_list|,
name|name
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|temp
init|=
name|job
operator|.
name|getLocalPath
argument_list|(
literal|"index/_"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
operator|new
name|Random
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"To index into "
operator|+
name|perm
argument_list|)
expr_stmt|;
comment|// delete old, if any
name|fs
operator|.
name|delete
argument_list|(
name|perm
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|IndexConfiguration
name|indexConf
init|=
operator|new
name|IndexConfiguration
argument_list|()
decl_stmt|;
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
name|String
name|analyzerName
init|=
name|indexConf
operator|.
name|getAnalyzerName
argument_list|()
decl_stmt|;
name|Analyzer
name|analyzer
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|analyzerClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|analyzerName
argument_list|)
decl_stmt|;
name|analyzer
operator|=
operator|(
name|Analyzer
operator|)
name|analyzerClass
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error in creating an analyzer object "
operator|+
name|analyzerName
argument_list|)
throw|;
block|}
comment|// build locally first
specifier|final
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|fs
operator|.
name|startLocalOutput
argument_list|(
name|perm
argument_list|,
name|temp
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|analyzer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// no delete, so no need for maxBufferedDeleteTerms
name|writer
operator|.
name|setMaxBufferedDocs
argument_list|(
name|indexConf
operator|.
name|getMaxBufferedDocs
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|setMaxFieldLength
argument_list|(
name|indexConf
operator|.
name|getMaxFieldLength
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|setMaxMergeDocs
argument_list|(
name|indexConf
operator|.
name|getMaxMergeDocs
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|setMergeFactor
argument_list|(
name|indexConf
operator|.
name|getMergeFactor
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|similarityName
init|=
name|indexConf
operator|.
name|getSimilarityName
argument_list|()
decl_stmt|;
if|if
condition|(
name|similarityName
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|similarityClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|similarityName
argument_list|)
decl_stmt|;
name|Similarity
name|similarity
init|=
operator|(
name|Similarity
operator|)
name|similarityClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|writer
operator|.
name|setSimilarity
argument_list|(
name|similarity
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error in creating a similarty object "
operator|+
name|similarityName
argument_list|)
throw|;
block|}
block|}
name|writer
operator|.
name|setUseCompoundFile
argument_list|(
name|indexConf
operator|.
name|isUseCompoundFile
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|LuceneDocumentWrapper
argument_list|>
argument_list|()
block|{
name|boolean
name|closed
decl_stmt|;
specifier|private
name|long
name|docCount
init|=
literal|0
decl_stmt|;
specifier|public
name|void
name|write
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|ImmutableBytesWritable
name|key
parameter_list|,
name|LuceneDocumentWrapper
name|value
parameter_list|)
throws|throws
name|IOException
block|{
comment|// unwrap and index doc
name|Document
name|doc
init|=
name|value
operator|.
name|get
argument_list|()
decl_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|docCount
operator|++
expr_stmt|;
name|progress
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|(
specifier|final
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// spawn a thread to give progress heartbeats
name|Thread
name|prog
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|closed
condition|)
block|{
try|try
block|{
name|reporter
operator|.
name|setStatus
argument_list|(
literal|"closing"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
continue|continue;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
return|return;
block|}
block|}
block|}
block|}
decl_stmt|;
try|try
block|{
name|prog
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// optimize index
if|if
condition|(
name|indexConf
operator|.
name|doOptimize
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Optimizing index."
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|optimize
argument_list|()
expr_stmt|;
block|}
comment|// close index
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Done indexing "
operator|+
name|docCount
operator|+
literal|" docs."
argument_list|)
expr_stmt|;
block|}
comment|// copy to perm destination in dfs
name|fs
operator|.
name|completeLocalOutput
argument_list|(
name|perm
argument_list|,
name|temp
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Copy done."
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|closed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

