begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Delete
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
name|Mutation
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
name|Put
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
name|input
operator|.
name|FileInputFormat
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
name|input
operator|.
name|SequenceFileInputFormat
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
comment|/**  * Import data written by {@link Export}.  */
end_comment

begin_class
specifier|public
class|class
name|Import
block|{
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"import"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CF_RENAME_PROP
init|=
literal|"HBASE_IMPORTER_RENAME_CFS"
decl_stmt|;
comment|/**    * Write table content out to files in hdfs.    */
specifier|static
class|class
name|Importer
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Mutation
argument_list|>
block|{
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|cfRenameMap
decl_stmt|;
comment|/**      * @param row  The current table row key.      * @param value  The columns.      * @param context  The current context.      * @throws IOException When something is broken with the data.      * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN,      *   org.apache.hadoop.mapreduce.Mapper.Context)      */
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|row
parameter_list|,
name|Result
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|writeResult
argument_list|(
name|row
argument_list|,
name|value
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeResult
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|result
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Put
name|put
init|=
literal|null
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|result
operator|.
name|raw
argument_list|()
control|)
block|{
if|if
condition|(
name|cfRenameMap
operator|!=
literal|null
condition|)
block|{
comment|// If there's a rename mapping for this CF, create a new KeyValue
name|byte
index|[]
name|newCfName
init|=
name|cfRenameMap
operator|.
name|get
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|newCfName
operator|!=
literal|null
condition|)
block|{
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
comment|// row buffer
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
comment|// row offset
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|,
comment|// row length
name|newCfName
argument_list|,
comment|// CF buffer
literal|0
argument_list|,
comment|// CF offset
name|newCfName
operator|.
name|length
argument_list|,
comment|// CF length
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
comment|// qualifier buffer
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
comment|// qualifier offset
name|kv
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
comment|// qualifier length
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|,
comment|// timestamp
name|KeyValue
operator|.
name|Type
operator|.
name|codeToType
argument_list|(
name|kv
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
comment|// KV Type
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
comment|// value buffer
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
comment|// value offset
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// value length
block|}
block|}
if|if
condition|(
name|kv
operator|.
name|isDelete
argument_list|()
condition|)
block|{
comment|// Deletes need to be written one-by-one,
comment|// since family deletes overwrite column(s) deletes
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
operator|new
name|Delete
argument_list|(
name|kv
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Puts are gathered into a single Put object
comment|// and written when finished
if|if
condition|(
name|put
operator|==
literal|null
condition|)
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|put
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
comment|// Make a map from sourceCfName to destCfName by parsing a config key
name|cfRenameMap
operator|=
literal|null
expr_stmt|;
name|String
name|allMappingsPropVal
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|CF_RENAME_PROP
argument_list|)
decl_stmt|;
if|if
condition|(
name|allMappingsPropVal
operator|!=
literal|null
condition|)
block|{
comment|// The conf value format should be sourceCf1:destCf1,sourceCf2:destCf2,...
name|String
index|[]
name|allMappings
init|=
name|allMappingsPropVal
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|mapping
range|:
name|allMappings
control|)
block|{
if|if
condition|(
name|cfRenameMap
operator|==
literal|null
condition|)
block|{
name|cfRenameMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|srcAndDest
init|=
name|mapping
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcAndDest
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
continue|continue;
block|}
name|cfRenameMap
operator|.
name|put
argument_list|(
name|srcAndDest
index|[
literal|0
index|]
operator|.
name|getBytes
argument_list|()
argument_list|,
name|srcAndDest
index|[
literal|1
index|]
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    *<p>Sets a configuration property with key {@link #CF_RENAME_PROP} in conf that tells    * the mapper how to rename column families.    *     *<p>Alternately, instead of calling this function, you could set the configuration key     * {@link #CF_RENAME_PROP} yourself. The value should look like     *<pre>srcCf1:destCf1,srcCf2:destCf2,....</pre>. This would have the same effect on    * the mapper behavior.    *     * @param conf the Configuration in which the {@link #CF_RENAME_PROP} key will be    *  set    * @param renameMap a mapping from source CF names to destination CF names    */
specifier|static
specifier|public
name|void
name|configureCfRenaming
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|renameMap
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|renameMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|sourceCf
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|destCf
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceCf
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
operator|||
name|sourceCf
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
operator|||
name|destCf
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
operator|||
name|destCf
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal character in CF names: "
operator|+
name|sourceCf
operator|+
literal|", "
operator|+
name|destCf
argument_list|)
throw|;
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|sourceCf
operator|+
literal|":"
operator|+
name|destCf
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|CF_RENAME_PROP
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets up the actual job.    *    * @param conf  The current configuration.    * @param args  The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
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
name|String
name|tableName
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|Path
name|inputDir
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|Importer
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|inputDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|Importer
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// No reducers.  Just write straight to table.  Call initTableReducerJob
comment|// because it sets up the TableOutputFormat.
name|TableMapReduceUtil
operator|.
name|initTableReducerJob
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
comment|/*    * @param errorMsg Error message.  Can be null.    */
specifier|private
specifier|static
name|void
name|usage
parameter_list|(
specifier|final
name|String
name|errorMsg
parameter_list|)
block|{
if|if
condition|(
name|errorMsg
operator|!=
literal|null
operator|&&
name|errorMsg
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: "
operator|+
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: Import<tablename><inputdir>"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Main entry point.    *    * @param args  The command line parameters.    * @throws Exception When running the job fails.    */
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
if|if
condition|(
name|otherArgs
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|usage
argument_list|(
literal|"Wrong number of arguments: "
operator|+
name|otherArgs
operator|.
name|length
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

