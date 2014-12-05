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
name|Arrays
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
name|Durability
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
name|coprocessor
operator|.
name|BaseRegionObserver
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|HRegion
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
name|TestImportTSVWithTTLs
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
name|TestImportTSVWithTTLs
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
comment|// We don't check persistence in HFiles in this test, but if we ever do we will
comment|// need this where the default hfile version is not 3 (i.e. 0.98)
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
literal|"hbase.coprocessor.region.classes"
argument_list|,
name|TTLCheckingObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|util
operator|.
name|startMiniMapReduceCluster
argument_list|()
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
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B,HBASE_CELL_TTL"
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
literal|"KEY\u001bVALUE1\u001bVALUE2\u001b1000000\n"
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
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
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|args
index|[
name|args
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
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
operator|.
name|getNameAsString
argument_list|()
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
try|try
block|{
comment|// Job will fail if observer rejects entries without TTL
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
block|}
finally|finally
block|{
comment|// Clean up
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
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tool
return|;
block|}
specifier|public
specifier|static
class|class
name|TTLCheckingObserver
extends|extends
name|BaseRegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|Put
name|put
parameter_list|,
name|WALEdit
name|edit
parameter_list|,
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
name|e
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaTable
argument_list|()
operator|&&
operator|!
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
comment|// The put carries the TTL attribute
if|if
condition|(
name|put
operator|.
name|getTTL
argument_list|()
operator|!=
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Operation does not have TTL set"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

