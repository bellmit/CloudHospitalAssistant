<template>
	<view class="page">
		<swiper indicator-dots="true">
			<swiper-item v-for="(img,key) in imgUrls" :key="key">
				<image :src="img" />
			</swiper-item>
		</swiper>
		<view class="example-title">科室介绍</view>
		<view class="uni-list">
			<view class="uni-list-cell" hover-class="uni-list-cell-hover" v-for="(value,key) in list" :key="key">
				<view class="uni-list-cell-navigate uni-navigate-right uni-media-list ">
					<view class="uni-media-list-logo">
						<image :src="value.img"></image>
					</view>
					<view class="uni-media-list-body">
						<view class="uni-media-list-text-top">{{value.title}}</view>
						<view class="uni-media-list-text-bottom uni-ellipsis">{{value.content}}</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				imgUrls: [
					"../../static/img/banner3.jpg",
					"../../static/img/banner1.jpg",
					"../../static/img/banner02.jpg"		
				],
				list: [{
						title: "心血管内科",
						content: "心血管内科是天津医科大学总医院临床重点发展学科之一，博士学位授权点。临床心血管病学尤其是介入心脏病学是国内领先中心之一。 1955年石毓澍、周金台开始开展心导管术，1960年周金台开展国内第一例左心导管检查术。历经石毓澍、周金台、石嘉玲、万征、孙跃民等几代科主任不懈努力，学术水平居国内前列。现任...",
						img: "../../static/img/deptIndex.png"
					},
					{
						title: "感染科",
						content: "天津医科大学总医院感染科是在原大内科传染病学组的基础上发展起来的。在半个多世纪的发展中，感染科实现了由传染病到感染病的工作转变。经过吴宗璘教授、邵维传教授和巩路教授三代老主任的不懈努力，感染科的学术影响力及学科规模不断扩大。目前感染科由逄崇杰担任科主任，下设感染病房、感染病门诊和发热/肠道门诊...",
						img: "../../static/img/deptIndex.png"
					},
					{
						title: "血液内科",
						content: "天津医科大学总医院血液内科成立至今60余年，现有3个病区，96张病床（包括层流病房16间，层流病床12张），年出院患者达2500余人次，年门急诊量逾4万人次。科室现有医护技人员115名，其中医生52人，护士55人，技术人员8人，正高级职称8人，副高级职称16人，博士生导师3人，硕士生导师9人。科室现有细胞形态学室、...",
						img: "../../static/img/deptIndex.png"
					},
					{
						title: "心胸外科",
						content: "天津医科大学总医院心胸外科创立于1946年，具有辉煌的历史。自199",
						img: "../../static/img/deptIndex.png"
					},
					{
						title: "胃肠肛肠外科",
						content: "胃肠肛肠外科专业以胃肠肿瘤，炎症性肠病以及肛周疾病作为主干疾病，在胃肿瘤、结直肿瘤和小肠肿瘤的手术和综合治疗上具有丰富的经验，肿瘤根治手术达到国际标准。按",
						img: "../../static/img/deptIndex.png"
					},
					{
						title: "儿科",
						content: "儿科于1946年建院之始成立，经过几十年的建设，如今已发展成为集儿童医疗、保健、科研、教学于一体，实力雄厚的学科。",
						img: "../../static/img/deptIndex.png"
					}
				]
			}
		},
		onLoad() {
			setTimeout(() => {
				this.showImg = true;
			}, 400)
		},
		methods: {

		}
	}
</script>

<style>
	image,
	swiper,
	.img-view {
		width: 750upx;
		height: 500upx;
	}
	.page-section-title{
		margin-top: 50upx;
	}
	.title {
		padding: 20upx;
	}
</style>
