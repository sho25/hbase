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
name|Random
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
name|math
operator|.
name|random
operator|.
name|RandomData
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
name|math
operator|.
name|random
operator|.
name|RandomDataImpl
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
name|io
operator|.
name|MapFile
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
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  *<p>  * This class runs performance benchmarks for {@link MapFile}.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|MapFilePerformanceEvaluation
block|{
specifier|private
specifier|static
specifier|final
name|int
name|ROW_LENGTH
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_COUNT
init|=
literal|1000000
decl_stmt|;
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|MapFilePerformanceEvaluation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
name|Text
name|format
parameter_list|(
specifier|final
name|int
name|i
parameter_list|,
specifier|final
name|Text
name|text
parameter_list|)
block|{
name|String
name|v
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|text
operator|.
name|set
argument_list|(
literal|"0000000000"
operator|.
name|substring
argument_list|(
name|v
operator|.
name|length
argument_list|()
argument_list|)
operator|+
name|v
argument_list|)
expr_stmt|;
return|return
name|text
return|;
block|}
specifier|private
name|void
name|runBenchmarks
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
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
name|mf
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
literal|"performanceevaluation.mapfile"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|mf
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|mf
argument_list|)
expr_stmt|;
block|}
name|runBenchmark
argument_list|(
operator|new
name|SequentialWriteBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|runBenchmark
argument_list|(
operator|new
name|UniformRandomReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|runBenchmark
argument_list|(
operator|new
name|GaussianRandomReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|runBenchmark
argument_list|(
operator|new
name|SequentialReadBenchmark
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|ROW_COUNT
argument_list|)
argument_list|,
name|ROW_COUNT
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runBenchmark
parameter_list|(
name|RowOrientedBenchmark
name|benchmark
parameter_list|,
name|int
name|rowCount
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running "
operator|+
name|benchmark
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" for "
operator|+
name|rowCount
operator|+
literal|" rows."
argument_list|)
expr_stmt|;
name|long
name|elapsedTime
init|=
name|benchmark
operator|.
name|run
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running "
operator|+
name|benchmark
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" for "
operator|+
name|rowCount
operator|+
literal|" rows took "
operator|+
name|elapsedTime
operator|+
literal|"ms."
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|abstract
class|class
name|RowOrientedBenchmark
block|{
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
specifier|final
name|Path
name|mf
decl_stmt|;
specifier|protected
specifier|final
name|int
name|totalRows
decl_stmt|;
specifier|protected
name|Text
name|key
decl_stmt|;
specifier|protected
name|Text
name|val
decl_stmt|;
specifier|public
name|RowOrientedBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|mf
operator|=
name|mf
expr_stmt|;
name|this
operator|.
name|totalRows
operator|=
name|totalRows
expr_stmt|;
name|this
operator|.
name|key
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|val
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
block|}
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// do nothing
block|}
specifier|abstract
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|protected
name|int
name|getReportingPeriod
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalRows
operator|/
literal|10
return|;
block|}
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// do nothing
block|}
comment|/**      * Run benchmark      * @return elapsed time.      * @throws Exception      */
name|long
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|elapsedTime
decl_stmt|;
name|setUp
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|totalRows
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
operator|&&
name|i
operator|%
name|getReportingPeriod
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Processed "
operator|+
name|i
operator|+
literal|" rows."
argument_list|)
expr_stmt|;
block|}
name|doRow
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|elapsedTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
expr_stmt|;
block|}
finally|finally
block|{
name|tearDown
argument_list|()
expr_stmt|;
block|}
return|return
name|elapsedTime
return|;
block|}
block|}
specifier|static
class|class
name|SequentialWriteBenchmark
extends|extends
name|RowOrientedBenchmark
block|{
specifier|protected
name|MapFile
operator|.
name|Writer
name|writer
decl_stmt|;
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|ROW_LENGTH
index|]
decl_stmt|;
specifier|public
name|SequentialWriteBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|writer
operator|=
operator|new
name|MapFile
operator|.
name|Writer
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
operator|.
name|toString
argument_list|()
argument_list|,
name|Text
operator|.
name|class
argument_list|,
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|val
operator|.
name|set
argument_list|(
name|generateValue
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|format
argument_list|(
name|i
argument_list|,
name|key
argument_list|)
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|generateValue
parameter_list|()
block|{
name|random
operator|.
name|nextBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getReportingPeriod
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalRows
return|;
comment|// don't report progress
block|}
annotation|@
name|Override
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
specifier|abstract
class|class
name|ReadBenchmark
extends|extends
name|RowOrientedBenchmark
block|{
specifier|protected
name|MapFile
operator|.
name|Reader
name|reader
decl_stmt|;
specifier|public
name|ReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|reader
operator|=
operator|new
name|MapFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|mf
operator|.
name|toString
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|SequentialReadBenchmark
extends|extends
name|ReadBenchmark
block|{
specifier|public
name|SequentialReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getReportingPeriod
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalRows
return|;
comment|// don't report progress
block|}
block|}
specifier|static
class|class
name|UniformRandomReadBenchmark
extends|extends
name|ReadBenchmark
block|{
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|UniformRandomReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|reader
operator|.
name|get
argument_list|(
name|getRandomRow
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Text
name|getRandomRow
parameter_list|()
block|{
return|return
name|format
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|totalRows
argument_list|)
argument_list|,
name|key
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|GaussianRandomReadBenchmark
extends|extends
name|ReadBenchmark
block|{
specifier|private
name|RandomData
name|randomData
init|=
operator|new
name|RandomDataImpl
argument_list|()
decl_stmt|;
specifier|public
name|GaussianRandomReadBenchmark
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|mf
parameter_list|,
name|int
name|totalRows
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|mf
argument_list|,
name|totalRows
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|doRow
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|int
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|reader
operator|.
name|get
argument_list|(
name|getGaussianRandomRow
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Text
name|getGaussianRandomRow
parameter_list|()
block|{
name|int
name|r
init|=
operator|(
name|int
operator|)
name|randomData
operator|.
name|nextGaussian
argument_list|(
name|totalRows
operator|/
literal|2
argument_list|,
name|totalRows
operator|/
literal|10
argument_list|)
decl_stmt|;
return|return
name|format
argument_list|(
name|r
argument_list|,
name|key
argument_list|)
return|;
block|}
block|}
comment|/**    * @param args    * @throws IOException     */
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
operator|new
name|MapFilePerformanceEvaluation
argument_list|()
operator|.
name|runBenchmarks
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

