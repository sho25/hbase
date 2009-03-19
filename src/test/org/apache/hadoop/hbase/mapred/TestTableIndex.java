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
name|File
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
name|Random
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestSuite
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|textui
operator|.
name|TestRunner
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
name|FileStatus
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
name|FileUtil
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
name|HRegion
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
name|client
operator|.
name|HTable
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
name|Scanner
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
name|HTableDescriptor
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
name|MultiRegionTable
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
name|JobClient
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
name|MiniMRCluster
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
name|lucene
operator|.
name|index
operator|.
name|Term
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
name|IndexSearcher
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
name|MultiSearcher
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
name|Searchable
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
name|Searcher
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
name|TermQuery
import|;
end_import

begin_comment
comment|/**  * Test Map/Reduce job to build index over HBase table  */
end_comment

begin_class
specifier|public
class|class
name|TestTableIndex
extends|extends
name|MultiRegionTable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestTableIndex
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"moretest"
decl_stmt|;
specifier|static
specifier|final
name|String
name|INPUT_COLUMN
init|=
literal|"contents:"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|TEXT_INPUT_COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|INPUT_COLUMN
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|OUTPUT_COLUMN
init|=
literal|"text:"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|TEXT_OUTPUT_COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|OUTPUT_COLUMN
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|ROWKEY_NAME
init|=
literal|"key"
decl_stmt|;
specifier|static
specifier|final
name|String
name|INDEX_DIR
init|=
literal|"testindex"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|columns
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|TEXT_INPUT_COLUMN
block|,
name|TEXT_OUTPUT_COLUMN
block|}
decl_stmt|;
specifier|private
name|JobConf
name|jobConf
init|=
literal|null
decl_stmt|;
comment|/** default constructor */
specifier|public
name|TestTableIndex
parameter_list|()
block|{
name|super
argument_list|(
name|INPUT_COLUMN
argument_list|)
expr_stmt|;
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|INPUT_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|OUTPUT_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|jobConf
operator|!=
literal|null
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
operator|new
name|File
argument_list|(
name|jobConf
operator|.
name|get
argument_list|(
literal|"hadoop.tmp.dir"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test HBase map/reduce    *     * @throws IOException    */
specifier|public
name|void
name|testTableIndex
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|printResults
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|printResults
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents before map/reduce"
argument_list|)
expr_stmt|;
block|}
name|scanTable
argument_list|(
name|printResults
argument_list|)
expr_stmt|;
name|MiniMRCluster
name|mrCluster
init|=
operator|new
name|MiniMRCluster
argument_list|(
literal|2
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|// set configuration parameter for index build
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.index.conf"
argument_list|,
name|createIndexConfContent
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|jobConf
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|TestTableIndex
operator|.
name|class
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setJobName
argument_list|(
literal|"index column contents"
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumMapTasks
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|// number of indexes to partition into
name|jobConf
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// use identity map (a waste, but just as an example)
name|IdentityTableMap
operator|.
name|initJob
argument_list|(
name|TABLE_NAME
argument_list|,
name|INPUT_COLUMN
argument_list|,
name|IdentityTableMap
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
comment|// use IndexTableReduce to build a Lucene index
name|jobConf
operator|.
name|setReducerClass
argument_list|(
name|IndexTableReduce
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|jobConf
argument_list|,
operator|new
name|Path
argument_list|(
name|INDEX_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setOutputFormat
argument_list|(
name|IndexOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|JobClient
operator|.
name|runJob
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|printResults
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents after map/reduce"
argument_list|)
expr_stmt|;
block|}
name|scanTable
argument_list|(
name|printResults
argument_list|)
expr_stmt|;
comment|// verify index results
name|verify
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|createIndexConfContent
parameter_list|()
block|{
name|StringBuffer
name|buffer
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<configuration><column><property>"
operator|+
literal|"<name>hbase.column.name</name><value>"
operator|+
name|INPUT_COLUMN
operator|+
literal|"</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.column.store</name> "
operator|+
literal|"<value>true</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.column.index</name>"
operator|+
literal|"<value>true</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.column.tokenize</name>"
operator|+
literal|"<value>false</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.column.boost</name>"
operator|+
literal|"<value>3</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.column.omit.norms</name>"
operator|+
literal|"<value>false</value></property></column>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.index.rowkey.name</name><value>"
operator|+
name|ROWKEY_NAME
operator|+
literal|"</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.index.max.buffered.docs</name>"
operator|+
literal|"<value>500</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.index.max.field.length</name>"
operator|+
literal|"<value>10000</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.index.merge.factor</name>"
operator|+
literal|"<value>10</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.index.use.compound.file</name>"
operator|+
literal|"<value>true</value></property>"
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"<property><name>hbase.index.optimize</name>"
operator|+
literal|"<value>true</value></property></configuration>"
argument_list|)
expr_stmt|;
name|IndexConfiguration
name|c
init|=
operator|new
name|IndexConfiguration
argument_list|()
decl_stmt|;
name|c
operator|.
name|addFromXML
argument_list|(
name|buffer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|c
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|scanTable
parameter_list|(
name|boolean
name|printResults
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|RowResult
name|r
range|:
name|scanner
control|)
block|{
if|if
condition|(
name|printResults
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"row: "
operator|+
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|e
range|:
name|r
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|printResults
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" column: "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|" value: "
operator|+
operator|new
name|String
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verify
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Force a cache flush for every online region to ensure that when the
comment|// scanner takes its snapshot, all the updates have made it into the cache.
for|for
control|(
name|HRegion
name|r
range|:
name|cluster
operator|.
name|getRegionThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|()
control|)
block|{
name|HRegionIncommon
name|region
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
name|Path
name|localDir
init|=
operator|new
name|Path
argument_list|(
name|getUnitTestdir
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|"index_"
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
name|this
operator|.
name|fs
operator|.
name|copyToLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|INDEX_DIR
argument_list|)
argument_list|,
name|localDir
argument_list|)
expr_stmt|;
name|FileSystem
name|localfs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|indexDirs
init|=
name|localfs
operator|.
name|listStatus
argument_list|(
name|localDir
argument_list|)
decl_stmt|;
name|Searcher
name|searcher
init|=
literal|null
decl_stmt|;
name|Scanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|indexDirs
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|searcher
operator|=
operator|new
name|IndexSearcher
argument_list|(
operator|(
operator|new
name|File
argument_list|(
name|indexDirs
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|)
operator|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexDirs
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|Searchable
index|[]
name|searchers
init|=
operator|new
name|Searchable
index|[
name|indexDirs
operator|.
name|length
index|]
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
name|indexDirs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|searchers
index|[
name|i
index|]
operator|=
operator|new
name|IndexSearcher
argument_list|(
operator|(
operator|new
name|File
argument_list|(
name|indexDirs
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
name|searcher
operator|=
operator|new
name|MultiSearcher
argument_list|(
name|searchers
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"no index directory found"
argument_list|)
throw|;
block|}
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
expr_stmt|;
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
name|conf
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
name|rowkeyName
init|=
name|indexConf
operator|.
name|getRowkeyName
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RowResult
name|r
range|:
name|scanner
control|)
block|{
name|String
name|value
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|Term
name|term
init|=
operator|new
name|Term
argument_list|(
name|rowkeyName
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|int
name|hitCount
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
argument_list|)
operator|.
name|length
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"check row "
operator|+
name|value
argument_list|,
literal|1
argument_list|,
name|hitCount
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Searcher.maxDoc: "
operator|+
name|searcher
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"IndexReader.numDocs: "
operator|+
operator|(
operator|(
name|IndexSearcher
operator|)
name|searcher
operator|)
operator|.
name|getIndexReader
argument_list|()
operator|.
name|numDocs
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|maxDoc
init|=
operator|(
operator|(
name|IndexSearcher
operator|)
name|searcher
operator|)
operator|.
name|getIndexReader
argument_list|()
operator|.
name|numDocs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"check number of rows"
argument_list|,
name|maxDoc
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
literal|null
operator|!=
name|searcher
condition|)
name|searcher
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|scanner
condition|)
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @param args unused    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|TestRunner
operator|.
name|run
argument_list|(
operator|new
name|TestSuite
argument_list|(
name|TestTableIndex
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

