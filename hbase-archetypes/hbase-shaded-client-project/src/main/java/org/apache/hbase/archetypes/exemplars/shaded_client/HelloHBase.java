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
name|hbase
operator|.
name|archetypes
operator|.
name|exemplars
operator|.
name|shaded_client
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
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
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
name|NamespaceDescriptor
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
name|NamespaceNotFoundException
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
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|Get
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
name|client
operator|.
name|Table
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

begin_comment
comment|/**  * Successful running of this application requires access to an active instance  * of HBase. For install instructions for a standalone instance of HBase, please  * refer to https://hbase.apache.org/book.html#quickstart  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HelloHBase
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|MY_NAMESPACE_NAME
init|=
literal|"myTestNamespace"
decl_stmt|;
specifier|static
specifier|final
name|TableName
name|MY_TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"myTestTable"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|MY_COLUMN_FAMILY_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|MY_FIRST_COLUMN_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myFirstColumn"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|MY_SECOND_COLUMN_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mySecondColumn"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|MY_ROW_ID
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowId01"
argument_list|)
decl_stmt|;
comment|// Private constructor included here to avoid checkstyle warnings
specifier|private
name|HelloHBase
parameter_list|()
block|{   }
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|boolean
name|deleteAllAtEOJ
init|=
literal|true
decl_stmt|;
comment|/**      * ConnectionFactory#createConnection() automatically looks for      * hbase-site.xml (HBase configuration parameters) on the system's      * CLASSPATH, to enable creation of Connection to HBase via ZooKeeper.      */
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|()
init|;
name|Admin
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|getClusterMetrics
argument_list|()
expr_stmt|;
comment|// assure connection successfully established
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n*** Hello HBase! -- Connection has been "
operator|+
literal|"established via ZooKeeper!!\n"
argument_list|)
expr_stmt|;
name|createNamespaceAndTable
argument_list|(
name|admin
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Getting a Table object for ["
operator|+
name|MY_TABLE_NAME
operator|+
literal|"] with which to perform CRUD operations in HBase."
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|MY_TABLE_NAME
argument_list|)
init|)
block|{
name|putRowToTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|getAndPrintRowContents
argument_list|(
name|table
argument_list|)
expr_stmt|;
if|if
condition|(
name|deleteAllAtEOJ
condition|)
block|{
name|deleteRow
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|deleteAllAtEOJ
condition|)
block|{
name|deleteNamespaceAndTable
argument_list|(
name|admin
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Invokes Admin#createNamespace and Admin#createTable to create a namespace    * with a table that has one column-family.    *    * @param admin Standard Admin object    * @throws IOException If IO problem encountered    */
specifier|static
name|void
name|createNamespaceAndTable
parameter_list|(
specifier|final
name|Admin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|namespaceExists
argument_list|(
name|admin
argument_list|,
name|MY_NAMESPACE_NAME
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Creating Namespace ["
operator|+
name|MY_NAMESPACE_NAME
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|MY_NAMESPACE_NAME
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|admin
operator|.
name|tableExists
argument_list|(
name|MY_TABLE_NAME
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Creating Table ["
operator|+
name|MY_TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"], with one Column Family ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_COLUMN_FAMILY_NAME
argument_list|)
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|MY_TABLE_NAME
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|MY_COLUMN_FAMILY_NAME
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Invokes Table#put to store a row (with two new columns created 'on the    * fly') into the table.    *    * @param table Standard Table object (used for CRUD operations).    * @throws IOException If IO problem encountered    */
specifier|static
name|void
name|putRowToTable
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|MY_ROW_ID
argument_list|)
operator|.
name|addColumn
argument_list|(
name|MY_COLUMN_FAMILY_NAME
argument_list|,
name|MY_FIRST_COLUMN_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Hello"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|MY_COLUMN_FAMILY_NAME
argument_list|,
name|MY_SECOND_COLUMN_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"World!"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Row ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_ROW_ID
argument_list|)
operator|+
literal|"] was put into Table ["
operator|+
name|table
operator|.
name|getName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"] in HBase;\n"
operator|+
literal|"  the row's two columns (created 'on the fly') are: ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_COLUMN_FAMILY_NAME
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_FIRST_COLUMN_QUALIFIER
argument_list|)
operator|+
literal|"] and ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_COLUMN_FAMILY_NAME
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_SECOND_COLUMN_QUALIFIER
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Invokes Table#get and prints out the contents of the retrieved row.    *    * @param table Standard Table object    * @throws IOException If IO problem encountered    */
specifier|static
name|void
name|getAndPrintRowContents
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|Result
name|row
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|MY_ROW_ID
argument_list|)
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Row ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"] was retrieved from Table ["
operator|+
name|table
operator|.
name|getName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"] in HBase, with the following content:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|colFamilyEntry
range|:
name|row
operator|.
name|getNoVersionMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|columnFamilyName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|colFamilyEntry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  Columns in Column Family ["
operator|+
name|columnFamilyName
operator|+
literal|"]:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|columnNameAndValueMap
range|:
name|colFamilyEntry
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    Value of Column ["
operator|+
name|columnFamilyName
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|columnNameAndValueMap
operator|.
name|getKey
argument_list|()
argument_list|)
operator|+
literal|"] == "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|columnNameAndValueMap
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Checks to see whether a namespace exists.    *    * @param admin Standard Admin object    * @param namespaceName Name of namespace    * @return true If namespace exists    * @throws IOException If IO problem encountered    */
specifier|static
name|boolean
name|namespaceExists
parameter_list|(
specifier|final
name|Admin
name|admin
parameter_list|,
specifier|final
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|admin
operator|.
name|getNamespaceDescriptor
argument_list|(
name|namespaceName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NamespaceNotFoundException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Invokes Table#delete to delete test data (i.e. the row)    *    * @param table Standard Table object    * @throws IOException If IO problem is encountered    */
specifier|static
name|void
name|deleteRow
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Deleting row ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|MY_ROW_ID
argument_list|)
operator|+
literal|"] from Table ["
operator|+
name|table
operator|.
name|getName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
operator|new
name|Delete
argument_list|(
name|MY_ROW_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Invokes Admin#disableTable, Admin#deleteTable, and Admin#deleteNamespace to    * disable/delete Table and delete Namespace.    *    * @param admin Standard Admin object    * @throws IOException If IO problem is encountered    */
specifier|static
name|void
name|deleteNamespaceAndTable
parameter_list|(
specifier|final
name|Admin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|MY_TABLE_NAME
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Disabling/deleting Table ["
operator|+
name|MY_TABLE_NAME
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|MY_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Disable a table before deleting it.
name|admin
operator|.
name|deleteTable
argument_list|(
name|MY_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|namespaceExists
argument_list|(
name|admin
argument_list|,
name|MY_NAMESPACE_NAME
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Deleting Namespace ["
operator|+
name|MY_NAMESPACE_NAME
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|MY_NAMESPACE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

