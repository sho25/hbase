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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|io
operator|.
name|PrintStream
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
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
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
name|cli
operator|.
name|CommandLineParser
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
name|cli
operator|.
name|HelpFormatter
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
name|cli
operator|.
name|Options
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
name|cli
operator|.
name|ParseException
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
name|cli
operator|.
name|PosixParser
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
operator|.
name|Reader
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
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_comment
comment|/**  * HLogPrettyPrinter prints the contents of a given HLog with a variety of  * options affecting formatting and extent of content.  *   * It targets two usage cases: pretty printing for ease of debugging directly by  * humans, and JSON output for consumption by monitoring and/or maintenance  * scripts.  *   * It can filter by row, region, or sequence id.  *   * It can also toggle output of values.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|HLogPrettyPrinter
block|{
specifier|private
name|boolean
name|outputValues
decl_stmt|;
specifier|private
name|boolean
name|outputJSON
decl_stmt|;
comment|// The following enable filtering by sequence, region, and row, respectively
specifier|private
name|long
name|sequence
decl_stmt|;
specifier|private
name|String
name|region
decl_stmt|;
specifier|private
name|String
name|row
decl_stmt|;
comment|// enable in order to output a single list of transactions from several files
specifier|private
name|boolean
name|persistentOutput
decl_stmt|;
specifier|private
name|boolean
name|firstTxn
decl_stmt|;
comment|// useful for programatic capture of JSON output
specifier|private
name|PrintStream
name|out
decl_stmt|;
comment|// for JSON encoding
specifier|private
name|ObjectMapper
name|mapper
decl_stmt|;
comment|/**    * Basic constructor that simply initializes values to reasonable defaults.    */
specifier|public
name|HLogPrettyPrinter
parameter_list|()
block|{
name|outputValues
operator|=
literal|false
expr_stmt|;
name|outputJSON
operator|=
literal|false
expr_stmt|;
name|sequence
operator|=
operator|-
literal|1
expr_stmt|;
name|region
operator|=
literal|null
expr_stmt|;
name|row
operator|=
literal|null
expr_stmt|;
name|persistentOutput
operator|=
literal|false
expr_stmt|;
name|firstTxn
operator|=
literal|true
expr_stmt|;
name|out
operator|=
name|System
operator|.
name|out
expr_stmt|;
name|mapper
operator|=
operator|new
name|ObjectMapper
argument_list|()
expr_stmt|;
block|}
comment|/**    * Fully specified constructor.    *     * @param outputValues    *          when true, enables output of values along with other log    *          information    * @param outputJSON    *          when true, enables output in JSON format rather than a    *          "pretty string"    * @param sequence    *          when nonnegative, serves as a filter; only log entries with this    *          sequence id will be printed    * @param region    *          when not null, serves as a filter; only log entries from this    *          region will be printed    * @param row    *          when not null, serves as a filter; only log entries from this row    *          will be printed    * @param persistentOutput    *          keeps a single list running for multiple files. if enabled, the    *          endPersistentOutput() method must be used!    * @param out    *          Specifies an alternative to stdout for the destination of this     *          PrettyPrinter's output.    */
specifier|public
name|HLogPrettyPrinter
parameter_list|(
name|boolean
name|outputValues
parameter_list|,
name|boolean
name|outputJSON
parameter_list|,
name|long
name|sequence
parameter_list|,
name|String
name|region
parameter_list|,
name|String
name|row
parameter_list|,
name|boolean
name|persistentOutput
parameter_list|,
name|PrintStream
name|out
parameter_list|)
block|{
name|this
operator|.
name|outputValues
operator|=
name|outputValues
expr_stmt|;
name|this
operator|.
name|outputJSON
operator|=
name|outputJSON
expr_stmt|;
name|this
operator|.
name|sequence
operator|=
name|sequence
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|persistentOutput
operator|=
name|persistentOutput
expr_stmt|;
if|if
condition|(
name|persistentOutput
condition|)
block|{
name|beginPersistentOutput
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|this
operator|.
name|firstTxn
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * turns value output on    */
specifier|public
name|void
name|enableValues
parameter_list|()
block|{
name|outputValues
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * turns value output off    */
specifier|public
name|void
name|disableValues
parameter_list|()
block|{
name|outputValues
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * turns JSON output on    */
specifier|public
name|void
name|enableJSON
parameter_list|()
block|{
name|outputJSON
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * turns JSON output off, and turns on "pretty strings" for human consumption    */
specifier|public
name|void
name|disableJSON
parameter_list|()
block|{
name|outputJSON
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * sets the region by which output will be filtered    *     * @param sequence    *          when nonnegative, serves as a filter; only log entries with this    *          sequence id will be printed    */
specifier|public
name|void
name|setSequenceFilter
parameter_list|(
name|long
name|sequence
parameter_list|)
block|{
name|this
operator|.
name|sequence
operator|=
name|sequence
expr_stmt|;
block|}
comment|/**    * sets the region by which output will be filtered    *     * @param region    *          when not null, serves as a filter; only log entries from this    *          region will be printed    */
specifier|public
name|void
name|setRegionFilter
parameter_list|(
name|String
name|region
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
block|}
comment|/**    * sets the region by which output will be filtered    *     * @param row    *          when not null, serves as a filter; only log entries from this row    *          will be printed    */
specifier|public
name|void
name|setRowFilter
parameter_list|(
name|String
name|row
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
comment|/**    * enables output as a single, persistent list. at present, only relevant in    * the case of JSON output.    */
specifier|public
name|void
name|beginPersistentOutput
parameter_list|()
block|{
if|if
condition|(
name|persistentOutput
condition|)
return|return;
name|persistentOutput
operator|=
literal|true
expr_stmt|;
name|firstTxn
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|outputJSON
condition|)
name|out
operator|.
name|print
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
block|}
comment|/**    * ends output of a single, persistent list. at present, only relevant in the    * case of JSON output.    */
specifier|public
name|void
name|endPersistentOutput
parameter_list|()
block|{
if|if
condition|(
operator|!
name|persistentOutput
condition|)
return|return;
name|persistentOutput
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|outputJSON
condition|)
name|out
operator|.
name|print
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
comment|/**    * reads a log file and outputs its contents, one transaction at a time, as    * specified by the currently configured options    *     * @param conf    *          the HBase configuration relevant to this log file    * @param p    *          the path of the log file to be read    * @throws IOException    *           may be unable to access the configured filesystem or requested    *           file.    */
specifier|public
name|void
name|processFile
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|isFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|p
operator|+
literal|" is not a file"
argument_list|)
throw|;
block|}
if|if
condition|(
name|outputJSON
operator|&&
operator|!
name|persistentOutput
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|firstTxn
operator|=
literal|true
expr_stmt|;
block|}
name|Reader
name|log
init|=
name|HLogFactory
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|FSHLog
operator|.
name|Entry
name|entry
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|log
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|HLogKey
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|WALEdit
name|edit
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
comment|// begin building a transaction structure
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|txn
init|=
name|key
operator|.
name|toStringMap
argument_list|()
decl_stmt|;
name|long
name|writeTime
init|=
name|key
operator|.
name|getWriteTime
argument_list|()
decl_stmt|;
comment|// check output filters
if|if
condition|(
name|sequence
operator|>=
literal|0
operator|&&
operator|(
operator|(
name|Long
operator|)
name|txn
operator|.
name|get
argument_list|(
literal|"sequence"
argument_list|)
operator|)
operator|!=
name|sequence
condition|)
continue|continue;
if|if
condition|(
name|region
operator|!=
literal|null
operator|&&
operator|!
operator|(
operator|(
name|String
operator|)
name|txn
operator|.
name|get
argument_list|(
literal|"region"
argument_list|)
operator|)
operator|.
name|equals
argument_list|(
name|region
argument_list|)
condition|)
continue|continue;
comment|// initialize list into which we will store atomic actions
name|List
argument_list|<
name|Map
argument_list|>
name|actions
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|edit
operator|.
name|getKeyValues
argument_list|()
control|)
block|{
comment|// add atomic operation to txn
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|op
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|(
name|kv
operator|.
name|toStringMap
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|outputValues
condition|)
name|op
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// check row output filter
if|if
condition|(
name|row
operator|==
literal|null
operator|||
operator|(
operator|(
name|String
operator|)
name|op
operator|.
name|get
argument_list|(
literal|"row"
argument_list|)
operator|)
operator|.
name|equals
argument_list|(
name|row
argument_list|)
condition|)
name|actions
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|actions
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
continue|continue;
name|txn
operator|.
name|put
argument_list|(
literal|"actions"
argument_list|,
name|actions
argument_list|)
expr_stmt|;
if|if
condition|(
name|outputJSON
condition|)
block|{
comment|// JSON output is a straightforward "toString" on the txn object
if|if
condition|(
name|firstTxn
condition|)
name|firstTxn
operator|=
literal|false
expr_stmt|;
else|else
name|out
operator|.
name|print
argument_list|(
literal|","
argument_list|)
expr_stmt|;
comment|// encode and print JSON
name|out
operator|.
name|print
argument_list|(
name|mapper
operator|.
name|writeValueAsString
argument_list|(
name|txn
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Pretty output, complete with indentation by atomic action
name|out
operator|.
name|println
argument_list|(
literal|"Sequence "
operator|+
name|txn
operator|.
name|get
argument_list|(
literal|"sequence"
argument_list|)
operator|+
literal|" "
operator|+
literal|"from region "
operator|+
name|txn
operator|.
name|get
argument_list|(
literal|"region"
argument_list|)
operator|+
literal|" "
operator|+
literal|"in table "
operator|+
name|txn
operator|.
name|get
argument_list|(
literal|"table"
argument_list|)
operator|+
literal|" at write timestamp: "
operator|+
operator|new
name|Date
argument_list|(
name|writeTime
argument_list|)
argument_list|)
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
name|actions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Map
name|op
init|=
name|actions
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"  Action:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"    row: "
operator|+
name|op
operator|.
name|get
argument_list|(
literal|"row"
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"    column: "
operator|+
name|op
operator|.
name|get
argument_list|(
literal|"family"
argument_list|)
operator|+
literal|":"
operator|+
name|op
operator|.
name|get
argument_list|(
literal|"qualifier"
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"    timestamp: "
operator|+
operator|(
operator|new
name|Date
argument_list|(
operator|(
name|Long
operator|)
name|op
operator|.
name|get
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
operator|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|outputValues
condition|)
name|out
operator|.
name|println
argument_list|(
literal|"    value: "
operator|+
name|op
operator|.
name|get
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|outputJSON
operator|&&
operator|!
name|persistentOutput
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
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
name|IOException
block|{
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
comment|/**    * Pass one or more log file names and formatting options and it will dump out    * a text version of the contents on<code>stdout</code>.    *     * @param args    *          Command line arguments    * @throws IOException    *           Thrown upon file system errors etc.    * @throws ParseException    *           Thrown if command-line parsing fails.    */
specifier|public
specifier|static
name|void
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create options
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"h"
argument_list|,
literal|"help"
argument_list|,
literal|false
argument_list|,
literal|"Output help message"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"j"
argument_list|,
literal|"json"
argument_list|,
literal|false
argument_list|,
literal|"Output JSON"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"p"
argument_list|,
literal|"printvals"
argument_list|,
literal|false
argument_list|,
literal|"Print values"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"r"
argument_list|,
literal|"region"
argument_list|,
literal|true
argument_list|,
literal|"Region to filter by. Pass region name; e.g. '.META.,,1'"
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"s"
argument_list|,
literal|"sequence"
argument_list|,
literal|true
argument_list|,
literal|"Sequence to filter by. Pass sequence number."
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
literal|"w"
argument_list|,
literal|"row"
argument_list|,
literal|true
argument_list|,
literal|"Row to filter by. Pass row name."
argument_list|)
expr_stmt|;
name|HLogPrettyPrinter
name|printer
init|=
operator|new
name|HLogPrettyPrinter
argument_list|()
decl_stmt|;
name|CommandLineParser
name|parser
init|=
operator|new
name|PosixParser
argument_list|()
decl_stmt|;
name|List
name|files
init|=
literal|null
decl_stmt|;
try|try
block|{
name|CommandLine
name|cmd
init|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|files
operator|=
name|cmd
operator|.
name|getArgList
argument_list|()
expr_stmt|;
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"h"
argument_list|)
condition|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"HLog<filename...>"
argument_list|,
name|options
argument_list|,
literal|true
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
comment|// configure the pretty printer using command line options
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"p"
argument_list|)
condition|)
name|printer
operator|.
name|enableValues
argument_list|()
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"j"
argument_list|)
condition|)
name|printer
operator|.
name|enableJSON
argument_list|()
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"r"
argument_list|)
condition|)
name|printer
operator|.
name|setRegionFilter
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"r"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"s"
argument_list|)
condition|)
name|printer
operator|.
name|setSequenceFilter
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"s"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"w"
argument_list|)
condition|)
name|printer
operator|.
name|setRowFilter
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"w"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"HFile filename(s) "
argument_list|,
name|options
argument_list|,
literal|true
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
comment|// get configuration, file system, and process the given files
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
expr_stmt|;
comment|// begin output
name|printer
operator|.
name|beginPersistentOutput
argument_list|()
expr_stmt|;
for|for
control|(
name|Object
name|f
range|:
name|files
control|)
block|{
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
operator|(
name|String
operator|)
name|f
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|file
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR, file doesnt exist: "
operator|+
name|file
argument_list|)
expr_stmt|;
return|return;
block|}
name|printer
operator|.
name|processFile
argument_list|(
name|conf
argument_list|,
name|file
argument_list|)
expr_stmt|;
block|}
name|printer
operator|.
name|endPersistentOutput
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

