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

begin_comment
comment|/**  * Tests HTable  */
end_comment

begin_class
specifier|public
class|class
name|TestHTable
extends|extends
name|HBaseClusterTestCase
implements|implements
name|HConstants
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
name|TestHTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HColumnDescriptor
name|column
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|nosuchTable
init|=
operator|new
name|Text
argument_list|(
literal|"nosuchTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|tableAname
init|=
operator|new
name|Text
argument_list|(
literal|"tableA"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|tableBname
init|=
operator|new
name|Text
argument_list|(
literal|"tableB"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
comment|/**    * the test    * @throws IOException    */
specifier|public
name|void
name|testHTable
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|value
init|=
literal|"value"
operator|.
name|getBytes
argument_list|(
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
try|try
block|{
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|nosuchTable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{
comment|// expected
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
name|HTableDescriptor
name|tableAdesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableAname
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|tableAdesc
operator|.
name|addFamily
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|tableBdesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableBname
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|tableBdesc
operator|.
name|addFamily
argument_list|(
name|column
argument_list|)
expr_stmt|;
comment|// create a couple of tables
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableAdesc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableBdesc
argument_list|)
expr_stmt|;
comment|// put some data into table A
name|HTable
name|a
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableAname
argument_list|)
decl_stmt|;
comment|// Assert the metadata is good.
name|HTableDescriptor
name|meta
init|=
name|a
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|meta
operator|.
name|equals
argument_list|(
name|tableAdesc
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|lockid
init|=
name|a
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|a
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|a
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
comment|// open a new connection to A and a connection to b
name|HTable
name|newA
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableAname
argument_list|)
decl_stmt|;
name|HTable
name|b
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableBname
argument_list|)
decl_stmt|;
comment|// copy data from A to B
name|HScannerInterface
name|s
init|=
name|newA
operator|.
name|obtainScanner
argument_list|(
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
name|lockid
operator|=
name|b
operator|.
name|startUpdate
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|b
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
name|b
operator|.
name|abort
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Opening a new connection to A will cause the tables to be reloaded
try|try
block|{
name|HTable
name|anotherA
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableAname
argument_list|)
decl_stmt|;
name|anotherA
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
comment|// We can still access A through newA because it has the table information
comment|// cached. And if it needs to recalibrate, that will cause the information
comment|// to be reloaded.
block|}
comment|/**     * For HADOOP-2579     */
specifier|public
name|void
name|testTableNotFoundExceptionWithoutAnyTables
parameter_list|()
block|{
try|try
block|{
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"notATable"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown a TableNotFoundException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{
comment|// expected
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown a TableNotFoundException instead of a "
operator|+
name|e
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**     * For HADOOP-2579     */
specifier|public
name|void
name|testTableNotFoundExceptionWithATable
parameter_list|()
block|{
try|try
block|{
name|HColumnDescriptor
name|column
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|testTableADesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|testTableADesc
operator|.
name|addFamily
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|testTableADesc
argument_list|)
expr_stmt|;
comment|// This should throw a TableNotFoundException, it has not been created
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"notATable"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown a TableNotFoundException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{
comment|// expected
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown a TableNotFoundException instead of a "
operator|+
name|e
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

