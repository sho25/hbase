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
name|text
operator|.
name|ParseException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
name|hbase
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
name|hbase
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
name|conf
operator|.
name|Configured
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
name|CellUtil
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
name|KeyValueUtil
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
name|TableName
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
name|wal
operator|.
name|WALKey
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
name|wal
operator|.
name|WALEdit
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
name|Mapper
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
name|hadoop
operator|.
name|util
operator|.
name|Tool
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
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * A tool to replay WAL files as a M/R job.  * The WAL can be replayed for a set of tables or all tables,  * and a timerange can be provided (in milliseconds).  * The WAL is filtered to the passed set of tables and  the output  * can optionally be mapped to another set of tables.  *  * WAL replay can also generate HFiles for later bulk importing,  * in that case the WAL is replayed for a single table only.  */
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
name|WALPlayer
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|WALPlayer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"WALPlayer"
decl_stmt|;
specifier|final
specifier|static
name|String
name|BULK_OUTPUT_CONF_KEY
init|=
literal|"wal.bulk.output"
decl_stmt|;
specifier|final
specifier|static
name|String
name|TABLES_KEY
init|=
literal|"wal.input.tables"
decl_stmt|;
specifier|final
specifier|static
name|String
name|TABLE_MAP_KEY
init|=
literal|"wal.input.tablesmap"
decl_stmt|;
comment|// This relies on Hadoop Configuration to handle warning about deprecated configs and
comment|// to set the correct non-deprecated configs when an old one shows up.
static|static
block|{
name|Configuration
operator|.
name|addDeprecation
argument_list|(
literal|"hlog.bulk.output"
argument_list|,
name|BULK_OUTPUT_CONF_KEY
argument_list|)
expr_stmt|;
name|Configuration
operator|.
name|addDeprecation
argument_list|(
literal|"hlog.input.tables"
argument_list|,
name|TABLES_KEY
argument_list|)
expr_stmt|;
name|Configuration
operator|.
name|addDeprecation
argument_list|(
literal|"hlog.input.tablesmap"
argument_list|,
name|TABLE_MAP_KEY
argument_list|)
expr_stmt|;
name|Configuration
operator|.
name|addDeprecation
argument_list|(
name|HLogInputFormat
operator|.
name|START_TIME_KEY
argument_list|,
name|WALInputFormat
operator|.
name|START_TIME_KEY
argument_list|)
expr_stmt|;
name|Configuration
operator|.
name|addDeprecation
argument_list|(
name|HLogInputFormat
operator|.
name|END_TIME_KEY
argument_list|,
name|WALInputFormat
operator|.
name|END_TIME_KEY
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
specifier|static
name|String
name|JOB_NAME_CONF_KEY
init|=
literal|"mapreduce.job.name"
decl_stmt|;
comment|/**    * A mapper that just writes out KeyValues.    * This one can be used together with {@link KeyValueSortReducer}    */
specifier|static
class|class
name|WALKeyValueMapper
extends|extends
name|Mapper
argument_list|<
name|WALKey
argument_list|,
name|WALEdit
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
block|{
specifier|private
name|byte
index|[]
name|table
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|WALKey
name|key
parameter_list|,
name|WALEdit
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
comment|// skip all other tables
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|table
argument_list|,
name|key
operator|.
name|getTablename
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|value
operator|.
name|getCells
argument_list|()
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|WALEdit
operator|.
name|isMetaEditFamily
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
condition|)
continue|continue;
name|context
operator|.
name|write
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|kv
argument_list|)
expr_stmt|;
block|}
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
throws|throws
name|IOException
block|{
comment|// only a single table is supported when HFiles are generated with HFileOutputFormat
name|String
name|tables
index|[]
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getStrings
argument_list|(
name|TABLES_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|==
literal|null
operator|||
name|tables
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
comment|// this can only happen when WALMapper is used directly by a class other than WALPlayer
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exactly one table must be specified for bulk HFile case."
argument_list|)
throw|;
block|}
name|table
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tables
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * A mapper that writes out {@link Mutation} to be directly applied to    * a running HBase instance.    */
specifier|static
class|class
name|WALMapper
extends|extends
name|Mapper
argument_list|<
name|WALKey
argument_list|,
name|WALEdit
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|Mutation
argument_list|>
block|{
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|TableName
argument_list|>
name|tables
init|=
operator|new
name|TreeMap
argument_list|<
name|TableName
argument_list|,
name|TableName
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|WALKey
name|key
parameter_list|,
name|WALEdit
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
if|if
condition|(
name|tables
operator|.
name|isEmpty
argument_list|()
operator|||
name|tables
operator|.
name|containsKey
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|)
condition|)
block|{
name|TableName
name|targetTable
init|=
name|tables
operator|.
name|isEmpty
argument_list|()
condition|?
name|key
operator|.
name|getTablename
argument_list|()
else|:
name|tables
operator|.
name|get
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|)
decl_stmt|;
name|ImmutableBytesWritable
name|tableOut
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|targetTable
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
literal|null
decl_stmt|;
name|Delete
name|del
init|=
literal|null
decl_stmt|;
name|Cell
name|lastCell
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|value
operator|.
name|getCells
argument_list|()
control|)
block|{
comment|// filtering WAL meta entries
if|if
condition|(
name|WALEdit
operator|.
name|isMetaEditFamily
argument_list|(
name|cell
operator|.
name|getFamily
argument_list|()
argument_list|)
condition|)
continue|continue;
comment|// A WALEdit may contain multiple operations (HBASE-3584) and/or
comment|// multiple rows (HBASE-5229).
comment|// Aggregate as much as possible into a single Put/Delete
comment|// operation before writing to the context.
if|if
condition|(
name|lastCell
operator|==
literal|null
operator|||
name|lastCell
operator|.
name|getTypeByte
argument_list|()
operator|!=
name|cell
operator|.
name|getTypeByte
argument_list|()
operator|||
operator|!
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|lastCell
argument_list|,
name|cell
argument_list|)
condition|)
block|{
comment|// row or type changed, write out aggregate KVs.
if|if
condition|(
name|put
operator|!=
literal|null
condition|)
name|context
operator|.
name|write
argument_list|(
name|tableOut
argument_list|,
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|del
operator|!=
literal|null
condition|)
name|context
operator|.
name|write
argument_list|(
name|tableOut
argument_list|,
name|del
argument_list|)
expr_stmt|;
if|if
condition|(
name|CellUtil
operator|.
name|isDelete
argument_list|(
name|cell
argument_list|)
condition|)
block|{
name|del
operator|=
operator|new
name|Delete
argument_list|(
name|cell
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|cell
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|CellUtil
operator|.
name|isDelete
argument_list|(
name|cell
argument_list|)
condition|)
block|{
name|del
operator|.
name|addDeleteMarker
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
name|lastCell
operator|=
name|cell
expr_stmt|;
block|}
comment|// write residual KVs
if|if
condition|(
name|put
operator|!=
literal|null
condition|)
name|context
operator|.
name|write
argument_list|(
name|tableOut
argument_list|,
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|del
operator|!=
literal|null
condition|)
name|context
operator|.
name|write
argument_list|(
name|tableOut
argument_list|,
name|del
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
throws|throws
name|IOException
block|{
name|String
index|[]
name|tableMap
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getStrings
argument_list|(
name|TABLE_MAP_KEY
argument_list|)
decl_stmt|;
name|String
index|[]
name|tablesToUse
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getStrings
argument_list|(
name|TABLES_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|tablesToUse
operator|==
literal|null
operator|||
name|tableMap
operator|==
literal|null
operator|||
name|tablesToUse
operator|.
name|length
operator|!=
name|tableMap
operator|.
name|length
condition|)
block|{
comment|// this can only happen when WALMapper is used directly by a class other than WALPlayer
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No tables or incorrect table mapping specified."
argument_list|)
throw|;
block|}
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|table
range|:
name|tablesToUse
control|)
block|{
name|tables
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableMap
index|[
name|i
operator|++
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param conf The {@link Configuration} to use.    */
specifier|public
name|WALPlayer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|void
name|setupTime
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|option
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|val
init|=
name|conf
operator|.
name|get
argument_list|(
name|option
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|val
condition|)
return|return;
name|long
name|ms
decl_stmt|;
try|try
block|{
comment|// first try to parse in user friendly form
name|ms
operator|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss.SS"
argument_list|)
operator|.
name|parse
argument_list|(
name|val
argument_list|)
operator|.
name|getTime
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|pe
parameter_list|)
block|{
try|try
block|{
comment|// then see if just a number of ms's was specified
name|ms
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|option
operator|+
literal|" must be specified either in the form 2001-02-20T16:35:06.99 "
operator|+
literal|"or as number of milliseconds"
argument_list|)
throw|;
block|}
block|}
name|conf
operator|.
name|setLong
argument_list|(
name|option
argument_list|,
name|ms
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets up the actual job.    *    * @param args  The command line parameters.    * @return The newly created job.    * @throws IOException When setting up the job fails.    */
specifier|public
name|Job
name|createSubmittableJob
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|setupTime
argument_list|(
name|conf
argument_list|,
name|HLogInputFormat
operator|.
name|START_TIME_KEY
argument_list|)
expr_stmt|;
name|setupTime
argument_list|(
name|conf
argument_list|,
name|HLogInputFormat
operator|.
name|END_TIME_KEY
argument_list|)
expr_stmt|;
name|Path
name|inputDir
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|String
index|[]
name|tables
init|=
name|args
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|String
index|[]
name|tableMap
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|tableMap
operator|=
name|args
index|[
literal|2
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableMap
operator|.
name|length
operator|!=
name|tables
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"The same number of tables and mapping must be provided."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// if not mapping is specified map each table to itself
name|tableMap
operator|=
name|tables
expr_stmt|;
block|}
name|conf
operator|.
name|setStrings
argument_list|(
name|TABLES_KEY
argument_list|,
name|tables
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|TABLE_MAP_KEY
argument_list|,
name|tableMap
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
name|Job
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|JOB_NAME_CONF_KEY
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|inputDir
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|WALPlayer
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
name|WALInputFormat
operator|.
name|class
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
if|if
condition|(
name|hfileOutPath
operator|!=
literal|null
condition|)
block|{
comment|// the bulk HFile case
if|if
condition|(
name|tables
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exactly one table must be specified for the bulk export option"
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
name|TableName
operator|.
name|valueOf
argument_list|(
name|tables
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|WALKeyValueMapper
operator|.
name|class
argument_list|)
expr_stmt|;
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
comment|// output to live cluster
name|job
operator|.
name|setMapperClass
argument_list|(
name|WALMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|MultiTableOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initCredentials
argument_list|(
name|job
argument_list|)
expr_stmt|;
comment|// No reducers.
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
literal|"Usage: "
operator|+
name|NAME
operator|+
literal|" [options]<wal inputdir><tables> [<tableMappings>]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Read all WAL entries for<tables>."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"If no tables (\"\") are specific, all tables are imported."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"(Careful, even -ROOT- and hbase:meta entries will be imported in that case.)"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Otherwise<tables> is a comma separated list of tables.\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"The WAL entries can be mapped to new set of tables via<tableMapping>."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"<tableMapping> is a command separated list of targettables."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"If specified, each table in<tables> must have a mapping.\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"By default "
operator|+
name|NAME
operator|+
literal|" will load data directly into HBase."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"To generate HFiles for a bulk data load instead, pass the option:"
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
literal|"  (Only one table can be specified, and no mapping is allowed!)"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Other options: (specify time range to WAL edit to consider)"
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
name|WALInputFormat
operator|.
name|START_TIME_KEY
operator|+
literal|"=[date|ms]"
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
name|WALInputFormat
operator|.
name|END_TIME_KEY
operator|+
literal|"=[date|ms]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"   -D "
operator|+
name|JOB_NAME_CONF_KEY
operator|+
literal|"=jobName - use the specified mapreduce job name for the wal player"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For performance also consider the following options:\n"
operator|+
literal|"  -Dmapreduce.map.speculative=false\n"
operator|+
literal|"  -Dmapreduce.reduce.speculative=false"
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
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|WALPlayer
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|otherArgs
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|getConf
argument_list|()
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
name|otherArgs
argument_list|)
decl_stmt|;
return|return
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
return|;
block|}
block|}
end_class

end_unit

