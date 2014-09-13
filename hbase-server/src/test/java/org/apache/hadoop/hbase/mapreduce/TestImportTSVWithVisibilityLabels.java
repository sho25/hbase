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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|conf
operator|.
name|Configurable
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
name|FSDataOutputStream
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
name|FileStatus
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
name|HBaseTestingUtility
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
name|testclassification
operator|.
name|LargeTests
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
name|testclassification
operator|.
name|MapReduceTests
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
name|ResultScanner
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsResponse
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
name|security
operator|.
name|User
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
name|security
operator|.
name|visibility
operator|.
name|Authorizations
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
name|security
operator|.
name|visibility
operator|.
name|CellVisibility
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
name|security
operator|.
name|visibility
operator|.
name|ScanLabelGenerator
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
name|security
operator|.
name|visibility
operator|.
name|SimpleScanLabelGenerator
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityClient
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityController
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityUtils
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
name|mapred
operator|.
name|Utils
operator|.
name|OutputFileUtils
operator|.
name|OutputFilesFilter
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestImportTSVWithVisibilityLabels
implements|implements
name|Configurable
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestImportTSVWithVisibilityLabels
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|NAME
init|=
name|TestImportTsv
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Delete the tmp directory after running doMROnTableTest. Boolean. Default is    * false.    */
specifier|protected
specifier|static
specifier|final
name|String
name|DELETE_AFTER_LOAD_CONF
init|=
name|NAME
operator|+
literal|".deleteAfterLoad"
decl_stmt|;
comment|/**    * Force use of combiner in doMROnTableTest. Boolean. Default is true.    */
specifier|protected
specifier|static
specifier|final
name|String
name|FORCE_COMBINER_CONF
init|=
name|NAME
operator|+
literal|".forceCombiner"
decl_stmt|;
specifier|private
specifier|final
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TOPSECRET
init|=
literal|"topsecret"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|PUBLIC
init|=
literal|"public"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|PRIVATE
init|=
literal|"private"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|CONFIDENTIAL
init|=
literal|"confidential"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SECRET
init|=
literal|"secret"
decl_stmt|;
specifier|private
specifier|static
name|User
name|SUPERUSER
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|util
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"setConf not supported"
argument_list|)
throw|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|provisionCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|util
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|SUPERUSER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"admin"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
literal|"admin,"
operator|+
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.master.classes"
argument_list|,
name|VisibilityController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.region.classes"
argument_list|,
name|VisibilityController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|VisibilityUtils
operator|.
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|,
name|SimpleScanLabelGenerator
operator|.
name|class
argument_list|,
name|ScanLabelGenerator
operator|.
name|class
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
comment|// Wait for the labels table to become available
name|util
operator|.
name|waitTableEnabled
argument_list|(
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|createLabels
argument_list|()
expr_stmt|;
name|Admin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|util
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|createLabels
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|labels
init|=
block|{
name|SECRET
block|,
name|TOPSECRET
block|,
name|CONFIDENTIAL
block|,
name|PUBLIC
block|,
name|PRIVATE
block|}
decl_stmt|;
try|try
block|{
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|conf
argument_list|,
name|labels
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added labels "
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in adding labels"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|releaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterMapper"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B,HBASE_CELL_VISIBILITY"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
name|tableName
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY\u001bVALUE1\u001bVALUE2\u001bsecret&private\n"
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTableWithDeletes
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterMapper"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B,HBASE_CELL_VISIBILITY"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
name|tableName
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY\u001bVALUE1\u001bVALUE2\u001bsecret&private\n"
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|issueDeleteAndVerifyData
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|issueDeleteAndVerifyData
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating table after delete."
argument_list|)
expr_stmt|;
name|Table
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
name|boolean
name|verified
init|=
literal|false
decl_stmt|;
name|long
name|pause
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|5
argument_list|)
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
name|numRetries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
literal|"private&secret"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Scan entire family.
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"secret"
argument_list|,
literal|"private"
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|resScanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|next
init|=
name|resScanner
operator|.
name|next
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|next
operator|.
name|length
argument_list|)
expr_stmt|;
name|verified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If here, a cell was empty. Presume its because updates came in
comment|// after the scanner had been opened. Wait a while and retry.
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|verified
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTableWithBulkload
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
name|Path
name|hfiles
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|hfiles
operator|.
name|toString
argument_list|()
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B,HBASE_CELL_VISIBILITY"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
name|tableName
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY\u001bVALUE1\u001bVALUE2\u001bsecret&private\n"
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkOutputWithTsvImporterTextMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
name|Path
name|bulkOutputPath
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterTextMapper"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B,HBASE_CELL_VISIBILITY"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|bulkOutputPath
operator|.
name|toString
argument_list|()
block|,
name|table
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY\u001bVALUE4\u001bVALUE8\u001bsecret&private\n"
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMRWithOutputFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"test-"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
decl_stmt|;
name|Path
name|hfiles
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|"hfiles"
argument_list|)
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterMapper"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"="
operator|+
name|hfiles
operator|.
name|toString
argument_list|()
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B,HBASE_CELL_VISIBILITY"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
name|tableName
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY\u001bVALUE4\u001bVALUE8\u001bsecret&private\n"
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|doMROnTableTest
argument_list|(
name|util
argument_list|,
name|FAMILY
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run an ImportTsv job and perform basic validation on the results. Returns    * the ImportTsv<code>Tool</code> instance so that other tests can inspect it    * for further validation as necessary. This method is static to insure    * non-reliance on instance's util/conf facilities.    *    * @param args    *          Any arguments to pass BEFORE inputFile path is appended.    * @return The Tool instance used to run the test.    */
specifier|protected
specifier|static
name|Tool
name|doMROnTableTest
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|family
parameter_list|,
name|String
name|data
parameter_list|,
name|String
index|[]
name|args
parameter_list|,
name|int
name|valueMultiplier
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|table
init|=
name|args
index|[
name|args
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// populate input file
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
name|Path
name|inputPath
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
argument_list|,
literal|"input.dat"
argument_list|)
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|op
init|=
name|fs
operator|.
name|create
argument_list|(
name|inputPath
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|data
operator|=
literal|"KEY\u001bVALUE1\u001bVALUE2\n"
expr_stmt|;
block|}
name|op
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|op
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Wrote test data to file: %s"
argument_list|,
name|inputPath
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|FORCE_COMBINER_CONF
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Forcing combiner."
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.map.combine.minspills"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// run the import
name|List
argument_list|<
name|String
argument_list|>
name|argv
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
decl_stmt|;
name|argv
operator|.
name|add
argument_list|(
name|inputPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Tool
name|tool
init|=
operator|new
name|ImportTsv
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running ImportTsv with arguments: "
operator|+
name|argv
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
name|tool
argument_list|,
name|argv
operator|.
name|toArray
argument_list|(
name|args
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Perform basic validation. If the input args did not include
comment|// ImportTsv.BULK_OUTPUT_CONF_KEY then validate data in the table.
comment|// Otherwise, validate presence of hfiles.
name|boolean
name|createdHFiles
init|=
literal|false
decl_stmt|;
name|String
name|outputPath
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|argv
control|)
block|{
if|if
condition|(
name|arg
operator|.
name|contains
argument_list|(
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
argument_list|)
condition|)
block|{
name|createdHFiles
operator|=
literal|true
expr_stmt|;
comment|// split '-Dfoo=bar' on '=' and keep 'bar'
name|outputPath
operator|=
name|arg
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
index|[
literal|1
index|]
expr_stmt|;
break|break;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"validating the table "
operator|+
name|createdHFiles
argument_list|)
expr_stmt|;
if|if
condition|(
name|createdHFiles
condition|)
name|validateHFiles
argument_list|(
name|fs
argument_list|,
name|outputPath
argument_list|,
name|family
argument_list|)
expr_stmt|;
else|else
name|validateTable
argument_list|(
name|conf
argument_list|,
name|table
argument_list|,
name|family
argument_list|,
name|valueMultiplier
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|DELETE_AFTER_LOAD_CONF
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting test subdirectory"
argument_list|)
expr_stmt|;
name|util
operator|.
name|cleanupDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
return|return
name|tool
return|;
block|}
comment|/**    * Confirm ImportTsv via HFiles on fs.    */
specifier|private
specifier|static
name|void
name|validateHFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|outputPath
parameter_list|,
name|String
name|family
parameter_list|)
throws|throws
name|IOException
block|{
comment|// validate number and content of output columns
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating HFiles."
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|configFamilies
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|configFamilies
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|foundFamilies
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|cfStatus
range|:
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
argument_list|,
operator|new
name|OutputFilesFilter
argument_list|()
argument_list|)
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"The output path has files"
argument_list|)
expr_stmt|;
name|String
index|[]
name|elements
init|=
name|cfStatus
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|cf
init|=
name|elements
index|[
name|elements
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|foundFamilies
operator|.
name|add
argument_list|(
name|cf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"HFile ouput contains a column family (%s) not present in input families (%s)"
argument_list|,
name|cf
argument_list|,
name|configFamilies
argument_list|)
argument_list|,
name|configFamilies
operator|.
name|contains
argument_list|(
name|cf
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|hfile
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|cfStatus
operator|.
name|getPath
argument_list|()
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"HFile %s appears to contain no data."
argument_list|,
name|hfile
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
name|hfile
operator|.
name|getLen
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Confirm ImportTsv via data in online table.    */
specifier|private
specifier|static
name|void
name|validateTable
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|family
parameter_list|,
name|int
name|valueMultiplier
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating table."
argument_list|)
expr_stmt|;
name|Table
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
name|boolean
name|verified
init|=
literal|false
decl_stmt|;
name|long
name|pause
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|5
argument_list|)
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
name|numRetries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Scan entire family.
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
literal|"secret"
argument_list|,
literal|"private"
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|resScanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
index|[]
name|next
init|=
name|resScanner
operator|.
name|next
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|next
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|resScanner
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Getting results "
operator|+
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|res
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
operator|+
name|valueMultiplier
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
operator|+
literal|2
operator|*
name|valueMultiplier
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Only one result set is expected, so let it loop.
block|}
name|verified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If here, a cell was empty. Presume its because updates came in
comment|// after the scanner had been opened. Wait a while and retry.
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|verified
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

