begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|tableindexed
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|ColumnNameParseException
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
name|HStoreKey
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
name|MasterNotRunningException
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
name|TableExistsException
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Extension of HBaseAdmin that creates indexed tables.  *   */
end_comment

begin_class
specifier|public
class|class
name|IndexedTableAdmin
extends|extends
name|HBaseAdmin
block|{
comment|/**    * Constructor    *     * @param conf Configuration object    * @throws MasterNotRunningException    */
specifier|public
name|IndexedTableAdmin
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a new table    *     * @param desc table descriptor for table    *     * @throws IllegalArgumentException if the table name is reserved    * @throws MasterNotRunningException if master is not running    * @throws TableExistsException if table already exists (If concurrent    * threads, the table may have been created between test-for-existence and    * attempt-at-creation).    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|this
operator|.
name|createIndexTables
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createIndexTables
parameter_list|(
name|HTableDescriptor
name|tableDesc
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|baseTableName
init|=
name|tableDesc
operator|.
name|getName
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexSpecification
name|indexSpec
range|:
name|tableDesc
operator|.
name|getIndexes
argument_list|()
control|)
block|{
name|HTableDescriptor
name|indexTableDesc
init|=
name|createIndexTableDesc
argument_list|(
name|baseTableName
argument_list|,
name|indexSpec
argument_list|)
decl_stmt|;
name|super
operator|.
name|createTable
argument_list|(
name|indexTableDesc
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HTableDescriptor
name|createIndexTableDesc
parameter_list|(
name|byte
index|[]
name|baseTableName
parameter_list|,
name|IndexSpecification
name|indexSpec
parameter_list|)
throws|throws
name|ColumnNameParseException
block|{
name|HTableDescriptor
name|indexTableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|indexSpec
operator|.
name|getIndexedTableName
argument_list|(
name|baseTableName
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|families
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|families
operator|.
name|add
argument_list|(
name|IndexedTable
operator|.
name|INDEX_COL_FAMILY
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|indexSpec
operator|.
name|getAllColumns
argument_list|()
control|)
block|{
name|families
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|HStoreKey
operator|.
name|getFamily
argument_list|(
name|column
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
name|HStoreKey
operator|.
name|COLUMN_FAMILY_DELIMITER
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|byte
index|[]
name|colFamily
range|:
name|families
control|)
block|{
name|indexTableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|colFamily
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|indexTableDesc
return|;
block|}
block|}
end_class

end_unit

