begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|classification
operator|.
name|InterfaceStability
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
name|ZooKeeperConnectionException
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
name|filter
operator|.
name|Filter
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
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKClusterId
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Import data written by {@link Export}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|Import
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
name|Import
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"import"
decl_stmt|;
specifier|final
specifier|static
name|String
name|CF_RENAME_PROP
init|=
literal|"HBASE_IMPORTER_RENAME_CFS"
decl_stmt|;
specifier|final
specifier|static
name|String
name|BULK_OUTPUT_CONF_KEY
init|=
literal|"import.bulk.output"
decl_stmt|;
specifier|final
specifier|static
name|String
name|FILTER_CLASS_CONF_KEY
init|=
literal|"import.filter.class"
decl_stmt|;
specifier|final
specifier|static
name|String
name|FILTER_ARGS_CONF_KEY
init|=
literal|"import.filter.args"
decl_stmt|;
comment|// Optional filter to use for mappers
specifier|private
specifier|static
name|Filter
name|filter
decl_stmt|;
comment|/**    * A mapper that just writes out KeyValues.    */
specifier|static
class|class
name|KeyValueImporter
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
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
for|for
control|(
name|KeyValue
name|kv
range|:
name|value
operator|.
name|raw
argument_list|()
control|)
block|{
name|kv
operator|=
name|filterKv
argument_list|(
name|kv
argument_list|)
expr_stmt|;
comment|// skip if we filtered it out
if|if
condition|(
name|kv
operator|==
literal|null
condition|)
continue|continue;
name|context
operator|.
name|write
argument_list|(
name|row
argument_list|,
name|convertKv
argument_list|(
name|kv
argument_list|,
name|cfRenameMap
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|cfRenameMap
operator|=
name|createCfRenameMap
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|=
name|instantiateFilter
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
specifier|private
name|UUID
name|clusterId
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
name|Delete
name|delete
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
name|kv
operator|=
name|filterKv
argument_list|(
name|kv
argument_list|)
expr_stmt|;
comment|// skip if we filter it out
if|if
condition|(
name|kv
operator|==
literal|null
condition|)
continue|continue;
name|kv
operator|=
name|convertKv
argument_list|(
name|kv
argument_list|,
name|cfRenameMap
argument_list|)
expr_stmt|;
comment|// Deletes and Puts are gathered and written when finished
if|if
condition|(
name|kv
operator|.
name|isDelete
argument_list|()
condition|)
block|{
if|if
condition|(
name|delete
operator|==
literal|null
condition|)
block|{
name|delete
operator|=
operator|new
name|Delete
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|delete
operator|.
name|addDeleteMarker
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|put
operator|.
name|setClusterId
argument_list|(
name|clusterId
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|delete
operator|!=
literal|null
condition|)
block|{
name|delete
operator|.
name|setClusterId
argument_list|(
name|clusterId
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|delete
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
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|cfRenameMap
operator|=
name|createCfRenameMap
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|filter
operator|=
name|instantiateFilter
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// TODO: This is kind of ugly doing setup of ZKW just to read the clusterid.
name|ZooKeeperWatcher
name|zkw
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
name|context
operator|.
name|getTaskAttemptID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|clusterId
operator|=
name|ZKClusterId
operator|.
name|getUUIDForCluster
argument_list|(
name|zkw
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperConnectionException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem connecting to ZooKeper during task setup"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem reading ZooKeeper data during task setup"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem setting up task"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|zkw
operator|!=
literal|null
condition|)
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Create a {@link Filter} to apply to all incoming keys ({@link KeyValue KeyValues}) to    * optionally not include in the job output    * @param conf {@link Configuration} from which to load the filter    * @return the filter to use for the task, or<tt>null</tt> if no filter to should be used    * @throws IllegalArgumentException if the filter is misconfigured    */
specifier|private
specifier|static
name|Filter
name|instantiateFilter
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// get the filter, if it was configured
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|filterClass
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|FILTER_CLASS_CONF_KEY
argument_list|,
literal|null
argument_list|,
name|Filter
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterClass
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No configured filter class, accepting all keyvalues."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempting to create filter:"
operator|+
name|filterClass
argument_list|)
expr_stmt|;
try|try
block|{
name|Method
name|m
init|=
name|filterClass
operator|.
name|getMethod
argument_list|(
literal|"createFilterFromArguments"
argument_list|,
name|ArrayList
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|(
name|Filter
operator|)
name|m
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|getFilterArgs
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't instantiate filter!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't instantiate filter!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't instantiate filter!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't instantiate filter!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't instantiate filter!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|getFilterArgs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|sargs
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|FILTER_ARGS_CONF_KEY
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|sargs
control|)
block|{
comment|// all the filters' instantiation methods expected quoted args since they are coming from
comment|// the shell, so add them here, though its shouldn't really be needed :-/
name|args
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"'"
operator|+
name|arg
operator|+
literal|"'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|args
return|;
block|}
comment|/**    * Attempt to filter out the keyvalue    * @param kv {@link KeyValue} on which to apply the filter    * @return<tt>null</tt> if the key should not be written, otherwise returns the original    *         {@link KeyValue}    */
specifier|private
specifier|static
name|KeyValue
name|filterKv
parameter_list|(
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
comment|// apply the filter and skip this kv if the filter doesn't apply
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|Filter
operator|.
name|ReturnCode
name|code
init|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter returned:"
operator|+
name|code
argument_list|)
expr_stmt|;
comment|// if its not an accept type, then skip this kv
if|if
condition|(
operator|!
operator|(
name|code
operator|.
name|equals
argument_list|(
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
operator|||
name|code
operator|.
name|equals
argument_list|(
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
argument_list|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping key: "
operator|+
name|kv
operator|+
literal|" from filter decision: "
operator|+
name|code
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
return|return
name|kv
return|;
block|}
comment|// helper: create a new KeyValue based on CF rename map
specifier|private
specifier|static
name|KeyValue
name|convertKv
parameter_list|(
name|KeyValue
name|kv
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|cfRenameMap
parameter_list|)
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
return|return
name|kv
return|;
block|}
comment|// helper: make a map from sourceCfName to destCfName by parsing a config key
specifier|private
specifier|static
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|createCfRenameMap
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|cfRenameMap
init|=
literal|null
decl_stmt|;
name|String
name|allMappingsPropVal
init|=
name|conf
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
return|return
name|cfRenameMap
return|;
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
comment|/**    * Add a Filter to be instantiated on import    * @param conf Configuration to update (will be passed to the job)    * @param clazz {@link Filter} subclass to instantiate on the server.    * @param args List of arguments to pass to the filter on instantiation    */
specifier|public
specifier|static
name|void
name|addFilterAndArguments
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|clazz
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|Import
operator|.
name|FILTER_CLASS_CONF_KEY
argument_list|,
name|clazz
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// build the param string for the key
name|StringBuilder
name|builder
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
name|args
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|arg
init|=
name|args
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|arg
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|!=
name|args
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
name|conf
operator|.
name|set
argument_list|(
name|Import
operator|.
name|FILTER_ARGS_CONF_KEY
argument_list|,
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets up the actual job.    * @param conf The current configuration.    * @param args The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
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
name|String
name|hfileOutPath
init|=
name|conf
operator|.
name|get
argument_list|(
name|BULK_OUTPUT_CONF_KEY
argument_list|)
decl_stmt|;
comment|// make sure we get the filter in the jars
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|filter
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|FILTER_CLASS_CONF_KEY
argument_list|,
literal|null
argument_list|,
name|Filter
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|conf
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
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
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|hfileOutPath
operator|!=
literal|null
condition|)
block|{
name|job
operator|.
name|setMapperClass
argument_list|(
name|KeyValueImporter
operator|.
name|class
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|KeyValueSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|outputDir
init|=
operator|new
name|Path
argument_list|(
name|hfileOutPath
argument_list|)
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|outputDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
expr_stmt|;
name|HFileOutputFormat
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// No reducers.  Just write straight to table.  Call initTableReducerJob
comment|// because it sets up the TableOutputFormat.
name|job
operator|.
name|setMapperClass
argument_list|(
name|Importer
operator|.
name|class
argument_list|)
expr_stmt|;
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
block|}
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
literal|"Usage: Import [options]<tablename><inputdir>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"By default Import will load data directly into HBase. To instead generate"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"HFiles of data to prepare for a bulk data load, pass the option:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -D"
operator|+
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"=/path/for/output"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" To apply a generic org.apache.hadoop.hbase.filter.Filter to the input, use"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -D"
operator|+
name|FILTER_CLASS_CONF_KEY
operator|+
literal|"=<name of filter class>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -D"
operator|+
name|FILTER_ARGS_CONF_KEY
operator|+
literal|"=<comma separated list of args for filter"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" NOTE: The filter will be applied BEFORE doing key renames via the "
operator|+
name|CF_RENAME_PROP
operator|+
literal|" property. Futher, filters will only use the"
operator|+
literal|"Filter#filterKeyValue(KeyValue) method to determine if the KeyValue should be added;"
operator|+
literal|" Filter.ReturnCode#INCLUDE and #INCLUDE_AND_NEXT_COL will be considered as including "
operator|+
literal|"the KeyValue."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For performance consider the following options:\n"
operator|+
literal|"  -Dmapred.map.tasks.speculative.execution=false\n"
operator|+
literal|"  -Dmapred.reduce.tasks.speculative.execution=false"
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

