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
name|compactions
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
name|MediumTests
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
name|HStore
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
name|StoreConfigInformation
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
name|StoreFile
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
name|ReflectionUtils
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
name|Collection
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|PerfTestCompactionPolicies
extends|extends
name|MockStoreFileGenerator
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
name|PerfTestCompactionPolicies
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RatioBasedCompactionPolicy
name|cp
decl_stmt|;
specifier|private
specifier|final
name|StoreFileListGenerator
name|generator
decl_stmt|;
specifier|private
specifier|final
name|HStore
name|store
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|StoreFileListGenerator
argument_list|>
name|fileGenClass
decl_stmt|;
specifier|private
specifier|final
name|int
name|max
decl_stmt|;
specifier|private
specifier|final
name|int
name|min
decl_stmt|;
specifier|private
specifier|final
name|float
name|ratio
decl_stmt|;
specifier|private
name|long
name|written
init|=
literal|0
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|Class
index|[]
name|policyClasses
init|=
operator|new
name|Class
index|[]
block|{
name|EverythingPolicy
operator|.
name|class
block|,
name|RatioBasedCompactionPolicy
operator|.
name|class
block|,
name|ExploringCompactionPolicy
operator|.
name|class
block|,     }
decl_stmt|;
name|Class
index|[]
name|fileListGenClasses
init|=
operator|new
name|Class
index|[]
block|{
name|ExplicitFileListGenerator
operator|.
name|class
block|,
name|ConstantSizeFileListGenerator
operator|.
name|class
block|,
name|SemiConstantSizeFileListGenerator
operator|.
name|class
block|,
name|GaussianFileListGenerator
operator|.
name|class
block|,
name|SinusoidalFileListGenerator
operator|.
name|class
block|,
name|SpikyFileListGenerator
operator|.
name|class
block|}
decl_stmt|;
name|int
index|[]
name|maxFileValues
init|=
operator|new
name|int
index|[]
block|{
literal|10
block|}
decl_stmt|;
name|int
index|[]
name|minFilesValues
init|=
operator|new
name|int
index|[]
block|{
literal|3
block|}
decl_stmt|;
name|float
index|[]
name|ratioValues
init|=
operator|new
name|float
index|[]
block|{
literal|1.2f
block|}
decl_stmt|;
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|(
name|maxFileValues
operator|.
name|length
operator|*
name|minFilesValues
operator|.
name|length
operator|*
name|fileListGenClasses
operator|.
name|length
operator|*
name|policyClasses
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
name|policyClass
range|:
name|policyClasses
control|)
block|{
for|for
control|(
name|Class
name|genClass
range|:
name|fileListGenClasses
control|)
block|{
for|for
control|(
name|int
name|maxFile
range|:
name|maxFileValues
control|)
block|{
for|for
control|(
name|int
name|minFile
range|:
name|minFilesValues
control|)
block|{
for|for
control|(
name|float
name|ratio
range|:
name|ratioValues
control|)
block|{
name|params
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|policyClass
block|,
name|genClass
block|,
name|maxFile
block|,
name|minFile
block|,
name|ratio
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|params
return|;
block|}
comment|/**    * Test the perf of a CompactionPolicy with settings.    * @param cpClass The compaction policy to test    * @param inMmax The maximum number of file to compact    * @param inMin The min number of files to compact    * @param inRatio The ratio that files must be under to be compacted.    */
specifier|public
name|PerfTestCompactionPolicies
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|CompactionPolicy
argument_list|>
name|cpClass
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|StoreFileListGenerator
argument_list|>
name|fileGenClass
parameter_list|,
specifier|final
name|int
name|inMmax
parameter_list|,
specifier|final
name|int
name|inMin
parameter_list|,
specifier|final
name|float
name|inRatio
parameter_list|)
throws|throws
name|IllegalAccessException
throws|,
name|InstantiationException
block|{
name|super
argument_list|(
name|PerfTestCompactionPolicies
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|fileGenClass
operator|=
name|fileGenClass
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|inMmax
expr_stmt|;
name|this
operator|.
name|min
operator|=
name|inMin
expr_stmt|;
name|this
operator|.
name|ratio
operator|=
name|inRatio
expr_stmt|;
comment|// Hide lots of logging so the system out is usable as a tab delimited file.
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
operator|.
name|getLogger
argument_list|(
name|CompactionConfiguration
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
operator|.
name|ERROR
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
operator|.
name|getLogger
argument_list|(
name|RatioBasedCompactionPolicy
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
operator|.
name|ERROR
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
operator|.
name|getLogger
argument_list|(
name|cpClass
argument_list|)
operator|.
name|setLevel
argument_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
operator|.
name|ERROR
argument_list|)
expr_stmt|;
name|Configuration
name|configuration
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Make sure that this doesn't include every file.
name|configuration
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.max"
argument_list|,
name|max
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
name|min
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|setFloat
argument_list|(
literal|"hbase.hstore.compaction.ratio"
argument_list|,
name|ratio
argument_list|)
expr_stmt|;
name|store
operator|=
name|createMockStore
argument_list|()
expr_stmt|;
name|this
operator|.
name|cp
operator|=
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|cpClass
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|,
name|StoreConfigInformation
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|configuration
block|,
name|store
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|generator
operator|=
name|fileGenClass
operator|.
name|newInstance
argument_list|()
expr_stmt|;
comment|// Used for making paths
block|}
annotation|@
name|Test
specifier|public
specifier|final
name|void
name|testSelection
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|fileDiff
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFileList
range|:
name|generator
control|)
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|currentFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
literal|18
argument_list|)
decl_stmt|;
for|for
control|(
name|StoreFile
name|file
range|:
name|storeFileList
control|)
block|{
name|currentFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
name|currentFiles
operator|=
name|runIteration
argument_list|(
name|currentFiles
argument_list|)
expr_stmt|;
block|}
name|fileDiff
operator|+=
operator|(
name|storeFileList
operator|.
name|size
argument_list|()
operator|-
name|currentFiles
operator|.
name|size
argument_list|()
operator|)
expr_stmt|;
block|}
comment|// print out tab delimited so that it can be used in excel/gdocs.
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|cp
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"\t"
operator|+
name|fileGenClass
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"\t"
operator|+
name|max
operator|+
literal|"\t"
operator|+
name|min
operator|+
literal|"\t"
operator|+
name|ratio
operator|+
literal|"\t"
operator|+
name|written
operator|+
literal|"\t"
operator|+
name|fileDiff
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|StoreFile
argument_list|>
name|runIteration
parameter_list|(
name|List
argument_list|<
name|StoreFile
argument_list|>
name|startingStoreFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|startingStoreFiles
argument_list|)
decl_stmt|;
name|CompactionRequest
name|req
init|=
name|cp
operator|.
name|selectCompaction
argument_list|(
name|storeFiles
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|long
name|newFileSize
init|=
literal|0
decl_stmt|;
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
init|=
name|req
operator|.
name|getFiles
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|filesToCompact
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|storeFiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|storeFiles
argument_list|)
expr_stmt|;
name|storeFiles
operator|.
name|removeAll
argument_list|(
name|filesToCompact
argument_list|)
expr_stmt|;
for|for
control|(
name|StoreFile
name|storeFile
range|:
name|filesToCompact
control|)
block|{
name|newFileSize
operator|+=
name|storeFile
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|storeFiles
operator|.
name|add
argument_list|(
name|createMockStoreFileBytes
argument_list|(
name|newFileSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|written
operator|+=
name|newFileSize
expr_stmt|;
return|return
name|storeFiles
return|;
block|}
specifier|private
name|HStore
name|createMockStore
parameter_list|()
block|{
name|HStore
name|s
init|=
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|s
operator|.
name|getStoreFileTtl
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|s
operator|.
name|getBlockingFileCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|7L
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
block|}
end_class

end_unit

