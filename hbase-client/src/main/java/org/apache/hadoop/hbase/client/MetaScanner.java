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
name|client
package|;
end_package

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
name|HRegionInfo
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
name|ServerName
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
name|HConnectionManager
operator|.
name|HConnectable
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
name|exceptions
operator|.
name|TableNotFoundException
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
name|util
operator|.
name|PairOfSameType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|NavigableMap
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

begin_comment
comment|/**  * Scanner class that contains the<code>.META.</code> table scanning logic  * and uses a Retryable scanner. Provided visitors will be called  * for each row.  *  * Although public visibility, this is not a public-facing API and may evolve in  * minor releases.  *  *<p> Note that during concurrent region splits, the scanner might not see  * META changes across rows (for parent and daughter entries) consistently.  * see HBASE-5986, and {@link DefaultMetaScannerVisitor} for details.</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaScanner
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
name|MetaScanner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Scans the meta table and calls a visitor on each RowResult and uses a empty    * start row value as table name.    *    * @param configuration conf    * @param visitor A custom visitor    * @throws IOException e    */
specifier|public
specifier|static
name|void
name|metaScan
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|MetaScannerVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|metaScan
argument_list|(
name|configuration
argument_list|,
name|visitor
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scans the meta table and calls a visitor on each RowResult. Uses a table    * name to locate meta regions.    *    * @param configuration config    * @param visitor visitor object    * @param userTableName User table name in meta table to start scan at.  Pass    * null if not interested in a particular table.    * @throws IOException e    */
specifier|public
specifier|static
name|void
name|metaScan
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|MetaScannerVisitor
name|visitor
parameter_list|,
name|byte
index|[]
name|userTableName
parameter_list|)
throws|throws
name|IOException
block|{
name|metaScan
argument_list|(
name|configuration
argument_list|,
name|visitor
argument_list|,
name|userTableName
argument_list|,
literal|null
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scans the meta table and calls a visitor on each RowResult. Uses a table    * name and a row name to locate meta regions. And it only scans at most    *<code>rowLimit</code> of rows.    *    * @param configuration HBase configuration.    * @param visitor Visitor object.    * @param userTableName User table name in meta table to start scan at.  Pass    * null if not interested in a particular table.    * @param row Name of the row at the user table. The scan will start from    * the region row where the row resides.    * @param rowLimit Max of processed rows. If it is less than 0, it    * will be set to default value<code>Integer.MAX_VALUE</code>.    * @throws IOException e    */
specifier|public
specifier|static
name|void
name|metaScan
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|MetaScannerVisitor
name|visitor
parameter_list|,
name|byte
index|[]
name|userTableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rowLimit
parameter_list|)
throws|throws
name|IOException
block|{
name|metaScan
argument_list|(
name|configuration
argument_list|,
name|visitor
argument_list|,
name|userTableName
argument_list|,
name|row
argument_list|,
name|rowLimit
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scans the meta table and calls a visitor on each RowResult. Uses a table    * name and a row name to locate meta regions. And it only scans at most    *<code>rowLimit</code> of rows.    *    * @param configuration HBase configuration.    * @param visitor Visitor object. Closes the visitor before returning.    * @param tableName User table name in meta table to start scan at.  Pass    * null if not interested in a particular table.    * @param row Name of the row at the user table. The scan will start from    * the region row where the row resides.    * @param rowLimit Max of processed rows. If it is less than 0, it    * will be set to default value<code>Integer.MAX_VALUE</code>.    * @param metaTableName Meta table to scan, root or meta.    * @throws IOException e    */
specifier|public
specifier|static
name|void
name|metaScan
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
specifier|final
name|MetaScannerVisitor
name|visitor
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|int
name|rowLimit
parameter_list|,
specifier|final
name|byte
index|[]
name|metaTableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|HConnectionManager
operator|.
name|execute
argument_list|(
operator|new
name|HConnectable
argument_list|<
name|Void
argument_list|>
argument_list|(
name|configuration
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Void
name|connect
parameter_list|(
name|HConnection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
name|metaScan
argument_list|(
name|conf
argument_list|,
name|connection
argument_list|,
name|visitor
argument_list|,
name|tableName
argument_list|,
name|row
argument_list|,
name|rowLimit
argument_list|,
name|metaTableName
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|visitor
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|metaScan
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|HConnection
name|connection
parameter_list|,
name|MetaScannerVisitor
name|visitor
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rowLimit
parameter_list|,
specifier|final
name|byte
index|[]
name|metaTableName
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|rowUpperLimit
init|=
name|rowLimit
operator|>
literal|0
condition|?
name|rowLimit
else|:
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// if row is not null, we want to use the startKey of the row's region as
comment|// the startRow for the meta scan.
name|byte
index|[]
name|startRow
decl_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
comment|// Scan starting at a particular row in a particular table
assert|assert
name|tableName
operator|!=
literal|null
assert|;
name|byte
index|[]
name|searchRow
init|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|HConstants
operator|.
name|NINES
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HTable
name|metaTable
init|=
literal|null
decl_stmt|;
try|try
block|{
name|metaTable
operator|=
operator|new
name|HTable
argument_list|(
name|configuration
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|Result
name|startRowResult
init|=
name|metaTable
operator|.
name|getRowOrBefore
argument_list|(
name|searchRow
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
if|if
condition|(
name|startRowResult
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
literal|"Cannot find row in .META. for table: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|", row="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|searchRow
argument_list|)
argument_list|)
throw|;
block|}
name|HRegionInfo
name|regionInfo
init|=
name|getHRegionInfo
argument_list|(
name|startRowResult
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HRegionInfo was null or empty in Meta for "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|", row="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|searchRow
argument_list|)
argument_list|)
throw|;
block|}
name|byte
index|[]
name|rowBefore
init|=
name|regionInfo
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|startRow
operator|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
name|rowBefore
argument_list|,
name|HConstants
operator|.
name|ZEROES
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|metaTable
operator|!=
literal|null
condition|)
block|{
name|metaTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|tableName
operator|==
literal|null
operator|||
name|tableName
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// Full META scan
name|startRow
operator|=
name|HConstants
operator|.
name|EMPTY_START_ROW
expr_stmt|;
block|}
else|else
block|{
comment|// Scan META for an entire table
name|startRow
operator|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|ZEROES
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Scan over each meta region
name|ScannerCallable
name|callable
decl_stmt|;
name|int
name|rows
init|=
name|Math
operator|.
name|min
argument_list|(
name|rowLimit
argument_list|,
name|configuration
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_META_SCANNER_CACHING
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_META_SCANNER_CACHING
argument_list|)
argument_list|)
decl_stmt|;
do|do
block|{
specifier|final
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|startRow
argument_list|)
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Scanning "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|metaTableName
argument_list|)
operator|+
literal|" starting at row="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startRow
argument_list|)
operator|+
literal|" for max="
operator|+
name|rowUpperLimit
operator|+
literal|" rows using "
operator|+
name|connection
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|callable
operator|=
operator|new
name|ScannerCallable
argument_list|(
name|connection
argument_list|,
name|metaTableName
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Open scanner
name|callable
operator|.
name|withRetries
argument_list|()
expr_stmt|;
name|int
name|processedRows
init|=
literal|0
decl_stmt|;
try|try
block|{
name|callable
operator|.
name|setCaching
argument_list|(
name|rows
argument_list|)
expr_stmt|;
name|done
label|:
do|do
block|{
if|if
condition|(
name|processedRows
operator|>=
name|rowUpperLimit
condition|)
block|{
break|break;
block|}
comment|//we have all the rows here
name|Result
index|[]
name|rrs
init|=
name|callable
operator|.
name|withRetries
argument_list|()
decl_stmt|;
if|if
condition|(
name|rrs
operator|==
literal|null
operator|||
name|rrs
operator|.
name|length
operator|==
literal|0
operator|||
name|rrs
index|[
literal|0
index|]
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
comment|//exit completely
block|}
for|for
control|(
name|Result
name|rr
range|:
name|rrs
control|)
block|{
if|if
condition|(
name|processedRows
operator|>=
name|rowUpperLimit
condition|)
block|{
break|break
name|done
break|;
block|}
if|if
condition|(
operator|!
name|visitor
operator|.
name|processRow
argument_list|(
name|rr
argument_list|)
condition|)
break|break
name|done
break|;
comment|//exit completely
name|processedRows
operator|++
expr_stmt|;
block|}
comment|//here, we didn't break anywhere. Check if we have more rows
block|}
do|while
condition|(
literal|true
condition|)
do|;
comment|// Advance the startRow to the end key of the current region
name|startRow
operator|=
name|callable
operator|.
name|getHRegionInfo
argument_list|()
operator|.
name|getEndKey
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// Close scanner
name|callable
operator|.
name|setClose
argument_list|()
expr_stmt|;
name|callable
operator|.
name|withRetries
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|startRow
argument_list|,
name|HConstants
operator|.
name|LAST_ROW
argument_list|)
operator|!=
literal|0
condition|)
do|;
block|}
comment|/**    * Returns HRegionInfo object from the column    * HConstants.CATALOG_FAMILY:HConstants.REGIONINFO_QUALIFIER of the catalog    * table Result.    * @param data a Result object from the catalog table scan    * @return HRegionInfo or null    */
specifier|public
specifier|static
name|HRegionInfo
name|getHRegionInfo
parameter_list|(
name|Result
name|data
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|data
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|HRegionInfo
name|info
init|=
name|HRegionInfo
operator|.
name|parseFromOrNull
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current INFO from scan results = "
operator|+
name|info
argument_list|)
expr_stmt|;
block|}
return|return
name|info
return|;
block|}
comment|/**    * Lists all of the regions currently in META.    * @param conf    * @return List of all user-space regions.    * @throws IOException    */
specifier|public
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|listAllRegions
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|listAllRegions
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Lists all of the regions currently in META.    * @param conf    * @param offlined True if we are to include offlined regions, false and we'll    * leave out offlined regions from returned list.    * @return List of all user-space regions.    * @throws IOException    */
specifier|public
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|listAllRegions
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|boolean
name|offlined
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
name|MetaScannerVisitor
name|visitor
init|=
operator|new
name|DefaultMetaScannerVisitor
argument_list|(
name|conf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|processRowInternal
parameter_list|(
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|result
operator|==
literal|null
operator|||
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|HRegionInfo
name|regionInfo
init|=
name|getHRegionInfo
argument_list|(
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Null REGIONINFO_QUALIFIER: "
operator|+
name|result
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// If region offline AND we are not to include offlined regions, return.
if|if
condition|(
name|regionInfo
operator|.
name|isOffline
argument_list|()
operator|&&
operator|!
name|offlined
condition|)
return|return
literal|true
return|;
name|regions
operator|.
name|add
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|metaScan
argument_list|(
name|conf
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
comment|/**    * Lists all of the table regions currently in META.    * @param conf    * @param offlined True if we are to include offlined regions, false and we'll    * leave out offlined regions from returned list.    * @return Map of all user-space regions to servers    * @throws IOException    */
specifier|public
specifier|static
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|allTableRegions
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|byte
index|[]
name|tablename
parameter_list|,
specifier|final
name|boolean
name|offlined
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regions
init|=
operator|new
name|TreeMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|()
decl_stmt|;
name|MetaScannerVisitor
name|visitor
init|=
operator|new
name|TableMetaScannerVisitor
argument_list|(
name|conf
argument_list|,
name|tablename
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|processRowInternal
parameter_list|(
name|Result
name|rowResult
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|info
init|=
name|getHRegionInfo
argument_list|(
name|rowResult
argument_list|)
decl_stmt|;
name|ServerName
name|serverName
init|=
name|HRegionInfo
operator|.
name|getServerName
argument_list|(
name|rowResult
argument_list|)
decl_stmt|;
name|regions
operator|.
name|put
argument_list|(
operator|new
name|UnmodifyableHRegionInfo
argument_list|(
name|info
argument_list|)
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|metaScan
argument_list|(
name|conf
argument_list|,
name|visitor
argument_list|,
name|tablename
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
comment|/**    * Visitor class called to process each row of the .META. table    */
specifier|public
interface|interface
name|MetaScannerVisitor
extends|extends
name|Closeable
block|{
comment|/**      * Visitor method that accepts a RowResult and the meta region location.      * Implementations can return false to stop the region's loop if it becomes      * unnecessary for some reason.      *      * @param rowResult result      * @return A boolean to know if it should continue to loop in the region      * @throws IOException e      */
specifier|public
name|boolean
name|processRow
parameter_list|(
name|Result
name|rowResult
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|MetaScannerVisitorBase
implements|implements
name|MetaScannerVisitor
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{     }
block|}
comment|/**    * A MetaScannerVisitor that provides a consistent view of the table's    * META entries during concurrent splits (see HBASE-5986 for details). This class    * does not guarantee ordered traversal of meta entries, and can block until the    * META entries for daughters are available during splits.    */
specifier|public
specifier|static
specifier|abstract
class|class
name|DefaultMetaScannerVisitor
extends|extends
name|MetaScannerVisitorBase
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|DefaultMetaScannerVisitor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|boolean
name|processRowInternal
parameter_list|(
name|Result
name|rowResult
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|boolean
name|processRow
parameter_list|(
name|Result
name|rowResult
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|info
init|=
name|getHRegionInfo
argument_list|(
name|rowResult
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|//skip over offline and split regions
if|if
condition|(
operator|!
operator|(
name|info
operator|.
name|isOffline
argument_list|()
operator|||
name|info
operator|.
name|isSplit
argument_list|()
operator|)
condition|)
block|{
return|return
name|processRowInternal
argument_list|(
name|rowResult
argument_list|)
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * A MetaScannerVisitor for a table. Provides a consistent view of the table's    * META entries during concurrent splits (see HBASE-5986 for details). This class    * does not guarantee ordered traversal of meta entries, and can block until the    * META entries for daughters are available during splits.    */
specifier|public
specifier|static
specifier|abstract
class|class
name|TableMetaScannerVisitor
extends|extends
name|DefaultMetaScannerVisitor
block|{
specifier|private
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|public
name|TableMetaScannerVisitor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|boolean
name|processRow
parameter_list|(
name|Result
name|rowResult
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|info
init|=
name|getHRegionInfo
argument_list|(
name|rowResult
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|Bytes
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableName
argument_list|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|super
operator|.
name|processRow
argument_list|(
name|rowResult
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

