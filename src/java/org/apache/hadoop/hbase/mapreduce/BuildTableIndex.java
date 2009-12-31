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
name|mapreduce
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
name|FileInputStream
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
name|conf
operator|.
name|Configuration
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
name|client
operator|.
name|Scan
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
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
name|util
operator|.
name|GenericOptionsParser
import|;
end_import

begin_comment
comment|/**  * Example table column indexing class.  Runs a mapreduce job to index  * specified table columns.  *<ul><li>Each row is modeled as a Lucene document: row key is indexed in  * its untokenized form, column name-value pairs are Lucene field name-value   * pairs.</li>  *<li>A file passed on command line is used to populate an  * {@link IndexConfiguration} which is used to set various Lucene parameters,  * specify whether to optimize an index and which columns to index and/or  * store, in tokenized or untokenized form, etc. For an example, see the  *<code>createIndexConfContent</code> method in TestTableIndex  *</li>  *<li>The number of reduce tasks decides the number of indexes (partitions).  * The index(es) is stored in the output path of job configuration.</li>  *<li>The index build process is done in the reduce phase. Users can use  * the map phase to join rows from different tables or to pre-parse/analyze  * column content, etc.</li>  *</ul>  */
end_comment

begin_class
specifier|public
class|class
name|BuildTableIndex
block|{
specifier|private
specifier|static
specifier|final
name|String
name|USAGE
init|=
literal|"Usage: BuildTableIndex "
operator|+
literal|"-r<numReduceTasks> -indexConf<iconfFile>\n"
operator|+
literal|"-indexDir<indexDir> -table<tableName>\n -columns<columnName1> "
operator|+
literal|"[<columnName2> ...]"
decl_stmt|;
comment|/**    * Prints the usage message and exists the program.    *     * @param message  The message to print first.    */
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|USAGE
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a new job.    * @param conf     *     * @param args  The command line arguments.    * @throws IOException When reading the configuration fails.    */
specifier|public
specifier|static
name|Job
name|createSubmittableJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|6
condition|)
block|{
name|printUsage
argument_list|(
literal|"Too few arguments"
argument_list|)
expr_stmt|;
block|}
name|int
name|numReduceTasks
init|=
literal|1
decl_stmt|;
name|String
name|iconfFile
init|=
literal|null
decl_stmt|;
name|String
name|indexDir
init|=
literal|null
decl_stmt|;
name|String
name|tableName
init|=
literal|null
decl_stmt|;
name|StringBuffer
name|columnNames
init|=
literal|null
decl_stmt|;
comment|// parse args
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
literal|"-r"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|numReduceTasks
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-indexConf"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|iconfFile
operator|=
name|args
index|[
operator|++
name|i
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-indexDir"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|indexDir
operator|=
name|args
index|[
operator|++
name|i
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-table"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|tableName
operator|=
name|args
index|[
operator|++
name|i
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-columns"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|columnNames
operator|=
operator|new
name|StringBuffer
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
while|while
condition|(
name|i
operator|+
literal|1
operator|<
name|args
operator|.
name|length
operator|&&
operator|!
name|args
index|[
name|i
operator|+
literal|1
index|]
operator|.
name|startsWith
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
name|columnNames
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|columnNames
operator|.
name|append
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|printUsage
argument_list|(
literal|"Unsupported option "
operator|+
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indexDir
operator|==
literal|null
operator|||
name|tableName
operator|==
literal|null
operator|||
name|columnNames
operator|==
literal|null
condition|)
block|{
name|printUsage
argument_list|(
literal|"Index directory, table name and at least one column must "
operator|+
literal|"be specified"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|iconfFile
operator|!=
literal|null
condition|)
block|{
comment|// set index configuration content from a file
name|String
name|content
init|=
name|readContent
argument_list|(
name|iconfFile
argument_list|)
decl_stmt|;
name|IndexConfiguration
name|iconf
init|=
operator|new
name|IndexConfiguration
argument_list|()
decl_stmt|;
comment|// purely to validate, exception will be thrown if not valid
name|iconf
operator|.
name|addFromXML
argument_list|(
name|content
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.index.conf"
argument_list|,
name|content
argument_list|)
expr_stmt|;
block|}
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"build index for table "
operator|+
name|tableName
argument_list|)
decl_stmt|;
comment|// number of indexes to partition into
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|numReduceTasks
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|columnName
range|:
name|columnNames
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
control|)
block|{
name|String
index|[]
name|fields
init|=
name|columnName
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|fields
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// use identity map (a waste, but just as an example)
name|IdentityTableMapper
operator|.
name|initJob
argument_list|(
name|tableName
argument_list|,
name|scan
argument_list|,
name|IdentityTableMapper
operator|.
name|class
argument_list|,
name|job
argument_list|)
expr_stmt|;
comment|// use IndexTableReduce to build a Lucene index
name|job
operator|.
name|setReducerClass
argument_list|(
name|IndexTableReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|indexDir
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|IndexOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
comment|/**    * Reads xml file of indexing configurations.  The xml format is similar to    * hbase-default.xml and hadoop-default.xml. For an example configuration,    * see the<code>createIndexConfContent</code> method in TestTableIndex.    *     * @param fileName  The file to read.    * @return XML configuration read from file.    * @throws IOException When the XML is broken.    */
specifier|private
specifier|static
name|String
name|readContent
parameter_list|(
name|String
name|fileName
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|int
name|length
init|=
operator|(
name|int
operator|)
name|file
operator|.
name|length
argument_list|()
decl_stmt|;
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
name|printUsage
argument_list|(
literal|"Index configuration file "
operator|+
name|fileName
operator|+
literal|" does not exist"
argument_list|)
expr_stmt|;
block|}
name|int
name|bytesRead
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|FileInputStream
name|fis
init|=
operator|new
name|FileInputStream
argument_list|(
name|file
argument_list|)
decl_stmt|;
try|try
block|{
comment|// read entire file into content
while|while
condition|(
name|bytesRead
operator|<
name|length
condition|)
block|{
name|int
name|read
init|=
name|fis
operator|.
name|read
argument_list|(
name|bytes
argument_list|,
name|bytesRead
argument_list|,
name|length
operator|-
name|bytesRead
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|>
literal|0
condition|)
block|{
name|bytesRead
operator|+=
name|read
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
finally|finally
block|{
name|fis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytesRead
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
return|;
block|}
comment|/**    * The main entry point.    *     * @param args  The command line arguments.    * @throws Exception When running the job fails.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
index|[]
name|otherArgs
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
operator|.
name|getRemainingArgs
argument_list|()
decl_stmt|;
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|otherArgs
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
condition|?
literal|0
else|:
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

