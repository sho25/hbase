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
name|metrics
package|;
end_package

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
name|SmallTests
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
name|MetricsTests
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableCounterLong
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
name|metrics2
operator|.
name|lib
operator|.
name|MutableGaugeLong
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
name|assertNull
import|;
end_import

begin_comment
comment|/**  *  Test of default BaseSource for hadoop 2  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MetricsTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestBaseSourceImpl
block|{
specifier|private
specifier|static
name|BaseSourceImpl
name|bmsi
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|bmsi
operator|=
operator|new
name|BaseSourceImpl
argument_list|(
literal|"TestName"
argument_list|,
literal|"test description"
argument_list|,
literal|"testcontext"
argument_list|,
literal|"TestContext"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetGauge
parameter_list|()
throws|throws
name|Exception
block|{
name|bmsi
operator|.
name|setGauge
argument_list|(
literal|"testset"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
operator|(
operator|(
name|MutableGaugeLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testset"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|bmsi
operator|.
name|setGauge
argument_list|(
literal|"testset"
argument_list|,
literal|300
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|300
argument_list|,
operator|(
operator|(
name|MutableGaugeLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testset"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncGauge
parameter_list|()
throws|throws
name|Exception
block|{
name|bmsi
operator|.
name|incGauge
argument_list|(
literal|"testincgauge"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
operator|(
operator|(
name|MutableGaugeLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testincgauge"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|bmsi
operator|.
name|incGauge
argument_list|(
literal|"testincgauge"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
operator|(
operator|(
name|MutableGaugeLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testincgauge"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecGauge
parameter_list|()
throws|throws
name|Exception
block|{
name|bmsi
operator|.
name|decGauge
argument_list|(
literal|"testdec"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|100
argument_list|,
operator|(
operator|(
name|MutableGaugeLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testdec"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|bmsi
operator|.
name|decGauge
argument_list|(
literal|"testdec"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|200
argument_list|,
operator|(
operator|(
name|MutableGaugeLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testdec"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncCounters
parameter_list|()
throws|throws
name|Exception
block|{
name|bmsi
operator|.
name|incCounters
argument_list|(
literal|"testinccounter"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
operator|(
operator|(
name|MutableCounterLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testinccounter"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|bmsi
operator|.
name|incCounters
argument_list|(
literal|"testinccounter"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
operator|(
operator|(
name|MutableCounterLong
operator|)
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testinccounter"
argument_list|)
operator|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveMetric
parameter_list|()
throws|throws
name|Exception
block|{
name|bmsi
operator|.
name|setGauge
argument_list|(
literal|"testrmgauge"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|bmsi
operator|.
name|removeMetric
argument_list|(
literal|"testrmgauge"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|bmsi
operator|.
name|metricsRegistry
operator|.
name|get
argument_list|(
literal|"testrmgauge"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

