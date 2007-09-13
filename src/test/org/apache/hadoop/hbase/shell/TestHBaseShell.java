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
name|shell
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|io
operator|.
name|UnsupportedEncodingException
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
name|HBaseAdmin
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
name|HBaseClusterTestCase
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
name|shell
operator|.
name|generated
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
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|generated
operator|.
name|Parser
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

begin_comment
comment|/**  * Tests for Hbase shell  */
end_comment

begin_class
specifier|public
class|class
name|TestHBaseShell
extends|extends
name|HBaseClusterTestCase
block|{
specifier|protected
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|baos
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|public
name|TestHBaseShell
parameter_list|()
block|{
name|super
argument_list|(
literal|1
comment|/*One region server only*/
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
comment|// Capture System.out so we can grep for stuff in it.  Have to do it once
comment|// only because ConsoleTable sets up STDOUT in a static initialization
name|this
operator|.
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|this
operator|.
name|baos
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create and then drop a table.    * Tests also that I can use single or double quotes around table and     * column family names.    * @throws Exception    */
specifier|public
name|void
name|testCreateDeleteTable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
specifier|final
name|String
name|columnFamily
init|=
name|tableName
decl_stmt|;
comment|// Create table
name|createTable
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" ("
operator|+
name|columnFamily
operator|+
literal|");"
argument_list|,
name|tableName
argument_list|,
name|columnFamily
argument_list|)
expr_stmt|;
comment|// Try describe
name|runCommand
argument_list|(
literal|"describe "
operator|+
name|tableName
operator|+
literal|";"
argument_list|)
expr_stmt|;
comment|// Try describe with single quotes
name|runCommand
argument_list|(
literal|"describe '"
operator|+
name|tableName
operator|+
literal|"';"
argument_list|)
expr_stmt|;
comment|// Try describe with double-quotes
name|runCommand
argument_list|(
literal|"describe \""
operator|+
name|tableName
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
comment|// Try dropping the table.
name|dropTable
argument_list|(
literal|"drop table "
operator|+
name|tableName
operator|+
literal|";"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Use double-quotes creating table.
specifier|final
name|String
name|dblQuoteSuffix
init|=
literal|"DblQuote"
decl_stmt|;
specifier|final
name|String
name|dblQuotedTableName
init|=
name|tableName
operator|+
name|dblQuoteSuffix
decl_stmt|;
name|createTable
argument_list|(
literal|"create table \""
operator|+
name|dblQuotedTableName
operator|+
literal|"\" ("
operator|+
name|columnFamily
operator|+
literal|");"
argument_list|,
name|dblQuotedTableName
argument_list|,
name|columnFamily
argument_list|)
expr_stmt|;
comment|// Use single-quotes creating table.
specifier|final
name|String
name|sglQuoteSuffix
init|=
literal|"SglQuote"
decl_stmt|;
specifier|final
name|String
name|snglQuotedTableName
init|=
name|tableName
operator|+
name|sglQuoteSuffix
decl_stmt|;
name|createTable
argument_list|(
literal|"create table '"
operator|+
name|snglQuotedTableName
operator|+
literal|"' ("
operator|+
name|columnFamily
operator|+
literal|");"
argument_list|,
name|snglQuotedTableName
argument_list|,
name|columnFamily
argument_list|)
expr_stmt|;
comment|// Use double-quotes around columnfamily name.
specifier|final
name|String
name|dblQuotedColumnFamily
init|=
name|columnFamily
operator|+
name|dblQuoteSuffix
decl_stmt|;
name|String
name|tmpTableName
init|=
name|tableName
operator|+
name|dblQuotedColumnFamily
decl_stmt|;
name|createTable
argument_list|(
literal|"create table "
operator|+
name|tmpTableName
operator|+
literal|" (\""
operator|+
name|dblQuotedColumnFamily
operator|+
literal|"\");"
argument_list|,
name|tmpTableName
argument_list|,
name|dblQuotedColumnFamily
argument_list|)
expr_stmt|;
comment|// Use single-quotes around columnfamily name.
specifier|final
name|String
name|sglQuotedColumnFamily
init|=
name|columnFamily
operator|+
name|sglQuoteSuffix
decl_stmt|;
name|tmpTableName
operator|=
name|tableName
operator|+
name|sglQuotedColumnFamily
expr_stmt|;
name|createTable
argument_list|(
literal|"create table "
operator|+
name|tmpTableName
operator|+
literal|" ('"
operator|+
name|sglQuotedColumnFamily
operator|+
literal|"');"
argument_list|,
name|tmpTableName
argument_list|,
name|sglQuotedColumnFamily
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testInsertSelectDelete
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
specifier|final
name|String
name|columnFamily
init|=
name|tableName
decl_stmt|;
name|createTable
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" ("
operator|+
name|columnFamily
operator|+
literal|");"
argument_list|,
name|tableName
argument_list|,
name|columnFamily
argument_list|)
expr_stmt|;
comment|// TODO: Add asserts that inserts, selects and deletes worked.
name|runCommand
argument_list|(
literal|"insert into "
operator|+
name|tableName
operator|+
literal|" ("
operator|+
name|columnFamily
operator|+
literal|") values ('"
operator|+
name|columnFamily
operator|+
literal|"') where row='"
operator|+
name|columnFamily
operator|+
literal|"';"
argument_list|)
expr_stmt|;
comment|// Insert with double-quotes on row.
name|runCommand
argument_list|(
literal|"insert into "
operator|+
name|tableName
operator|+
literal|" ("
operator|+
name|columnFamily
operator|+
literal|") values ('"
operator|+
name|columnFamily
operator|+
literal|"') where row=\""
operator|+
name|columnFamily
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
comment|// Insert with double-quotes on row and value.
name|runCommand
argument_list|(
literal|"insert into "
operator|+
name|tableName
operator|+
literal|" ("
operator|+
name|columnFamily
operator|+
literal|") values (\""
operator|+
name|columnFamily
operator|+
literal|"\") where row=\""
operator|+
name|columnFamily
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
name|runCommand
argument_list|(
literal|"select \""
operator|+
name|columnFamily
operator|+
literal|"\" from \""
operator|+
name|tableName
operator|+
literal|"\" where row=\""
operator|+
name|columnFamily
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
name|runCommand
argument_list|(
literal|"delete \""
operator|+
name|columnFamily
operator|+
literal|":\" from \""
operator|+
name|tableName
operator|+
literal|"\" where row=\""
operator|+
name|columnFamily
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createTable
parameter_list|(
specifier|final
name|String
name|cmdStr
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|String
name|columnFamily
parameter_list|)
throws|throws
name|ParseException
throws|,
name|IOException
block|{
comment|// Run create command.
name|runCommand
argument_list|(
name|cmdStr
argument_list|)
expr_stmt|;
comment|// Assert table was created.
name|assertTrue
argument_list|(
name|this
operator|.
name|admin
operator|.
name|tableExists
argument_list|(
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|HTableDescriptor
index|[]
name|tables
init|=
name|this
operator|.
name|admin
operator|.
name|listTables
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|td
init|=
literal|null
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
name|tables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|tables
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|td
operator|=
name|tables
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|td
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|td
operator|.
name|hasFamily
argument_list|(
operator|new
name|Text
argument_list|(
name|columnFamily
operator|+
literal|":"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|dropTable
parameter_list|(
specifier|final
name|String
name|cmdStr
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|ParseException
throws|,
name|IOException
block|{
name|runCommand
argument_list|(
name|cmdStr
argument_list|)
expr_stmt|;
comment|// Assert its gone
name|HTableDescriptor
index|[]
name|tables
init|=
name|this
operator|.
name|admin
operator|.
name|listTables
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
name|tables
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertNotSame
argument_list|(
name|tableName
argument_list|,
name|tables
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ReturnMsg
name|runCommand
parameter_list|(
specifier|final
name|String
name|cmdStr
parameter_list|)
throws|throws
name|ParseException
throws|,
name|UnsupportedEncodingException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running command: "
operator|+
name|cmdStr
argument_list|)
expr_stmt|;
name|Parser
name|parser
init|=
operator|new
name|Parser
argument_list|(
name|cmdStr
argument_list|)
decl_stmt|;
name|Command
name|cmd
init|=
name|parser
operator|.
name|terminatedCommand
argument_list|()
decl_stmt|;
name|ReturnMsg
name|rm
init|=
name|cmd
operator|.
name|execute
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|dumpStdout
argument_list|()
expr_stmt|;
return|return
name|rm
return|;
block|}
specifier|private
name|void
name|dumpStdout
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"STDOUT: "
operator|+
operator|new
name|String
argument_list|(
name|this
operator|.
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

